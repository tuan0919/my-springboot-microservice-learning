
# Một số ghi chú cho bản thân:
## API Gateway
API Gateway dùng để ẩn đi endpoint thật sự của hệ thống và đóng vai trò như một gateway mà user bắt buộc phải request đến trước khi đến với bất kì endpoint nào.

Cấu trúc mẫu của một file .yaml dùng để config API gateway:

```yaml
server:
  port: 8888

app:
  api-prefix: /api/v1

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        - id: exclude
          uri: no://op
          predicates:
            - Path=${app.api-prefix}/profile/internal/**
            - Path=${app.api-prefix}/profile/internal
          filters:
            - StripPrefix=2
            - SetStatus=UNAUTHORIZED
        - id: indentity_service
          uri: http://localhost:8080
          predicates:
            - Path=${app.api-prefix}/identity/**
          filters:
            - StripPrefix=2
        - id: profile_service
          uri: http://localhost:8081
          predicates:
            - Path=${app.api-prefix}/profile/**
          filters:
            - StripPrefix=2
```
Để sử dụng Global Filter trong API Gateway, implement lại hai interface GlobalFilter, Ordered sau đó override lại hàm này:
```java
@Override
// Có thể xem đối tượng Mono này như một Promise trong JS
// Bên cạnh đối tượng Mono còn có Flux
// Mono.subscribe(result -> {...}) sẽ gọi sau khi đối tượng Mono có dữ liệu
public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain){
    ...
}

// Thứ tự chạy của Filter này, -1 là ưu tiên chạy đầu tiên
@Override
public int getOrder() {
    return -1;
}
```
Common practice là thực hiện Authentication ở API Gateway (có thể sử dụng Identity Service bên dưới để thực hiện quá trình này) và Authorization ở từng Microservice riêng lẻ.

Ưu điểm là dễ bảo trì và mở rộng các microservice.

Nhược điểm của pattern này sẽ khiến cho các Microservice phải lặp đi lặp lại một code base dùng để config JWTDecoder và SecurityConfig.

![Microservice structure](https://i.imgur.com/lfls0Lg.png)

## Communicate between microservices
Giữa các microservices phải có cách để trao đổi thông tin với nhau (microservice này có thể gọi đến microservice khác trong hệ thống). Có thể sử dụng thư viện OpenFeign của Spring Cloud

```java
@FeignClient(name = "profile-service",
        url = "${app.services.profile}",
        configuration = {AuthenticationRequestInterceptor.class})
public interface ProfileClient {
    @PostMapping(value = "/internal/*", produces = MediaType.APPLICATION_JSON_VALUE)
    UserProfileResponse createProfile(@RequestBody ProfileCreationRequest profile);
}
```

Hoặc sử dụng trực tiếp Reactor Core trong Spring Boot:

```java
public interface IdentityClient {
    @PostExchange(url = "/auth/introspect", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<ApiResponse<IntrospectResponse>> introspect(@RequestBody IntrospectRequest request);
}

// Đoạn này phải xây dựng Client thủ công
@Configuration
public class WebClientConfiguration {
    @Bean
    WebClient webClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8080/identity")
                .build();
    }

    @Bean
    IdentityClient identityClient(WebClient webClient) {
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClient)).build();
        return httpServiceProxyFactory.createClient(IdentityClient.class);
    }
}
```

Khi thực hiện giao tiếp giữa các internal endpoint, phải đảm bảo chúng share HttpRequestHeader cho nhau, để các microservice có thể trích xuất thông tin JWT từ Header.

Đối với cách tiếp cận sử dụng OpenFeign, có thể share header bằng cách implement một Interceptor mà sẽ chạy trước khi request đến microservice khác

```java
@Slf4j
public class AuthenticationRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        var servletRequestAttr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        var authHeader = servletRequestAttr.getRequest().getHeader("Authorization");
        log.info("Header: {}", authHeader);
        if (StringUtils.hasText(authHeader)) {
            requestTemplate.header("Authorization", authHeader);
        }
    }
}
```

## My Security Config
Cách tiếp cận thủ công là sử dụng thư viện cũ như io.jsonwebtoken để thực hiện bảo mật bằng JWT sau đó viết thủ công một lớp Filter để thực hiện Authentication. Còn Authorization sẽ thực hiện trực tiếp tại lớp SecurityConfig luôn.

```java
  public SecurityFilterChain privateFilterChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
            .csrf(AbstractHttpConfigurer::disable)
            .securityMatcher("/api/v2/**")
            .authorizeHttpRequests(auth -> {
              auth.requestMatchers("/api/v2/gio-hang/**")
                      .hasAnyAuthority(ERole.ROLE_USER.name(), ERole.ROLE_ADMIN.name())
                  .requestMatchers(HttpMethod.POST, "/api/v2/dat-hang")
                      .hasAnyAuthority(ERole.ROLE_USER.name(), ERole.ROLE_ADMIN.name())
                  .requestMatchers(HttpMethod.GET,"/api/v2/don-hang/**")
                      .hasAnyAuthority(ERole.ROLE_USER.name(), ERole.ROLE_ADMIN.name())
                  .requestMatchers("/api/v2/yeu-thich/**")
                      .hasAnyAuthority(ERole.ROLE_USER.name(), ERole.ROLE_ADMIN.name())
                  .requestMatchers("/api/v2/nguoi-dung/**")
                      .hasAnyAuthority(ERole.ROLE_USER.name(), ERole.ROLE_ADMIN.name())
                  .anyRequest()
                      .denyAll();
            })
            .sessionManagement(sessionManager -> sessionManager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(daoAuthenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    return httpSecurity.build();
  }

  // Filter để thực hiện Authentication và set thủ công cho SecurityContextHolder
  public class JwtAuthFilter extends HttpFilter {

  private final JwtService jwtService;
  private final UserDetailsServiceImpl userDetailsService;

  @Qualifier("handlerExceptionResolver")
  public void setResolver(HandlerExceptionResolver resolver) {
    this.resolver = resolver;
  }

  @Override
  protected void doFilter(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {
    ...
    if (jwtService.validateToken(token, userDetails)) {
          UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                  userDetails, null, userDetails.getAuthorities());
          authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
    ...
}
```

Cách tiếp cận nhanh gọn hơn là sử dụng thư viện `spring-boot-starter-oauth2-resource-server`

```pom
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
		</dependency>
```

Thư viện này hỗ trợ rất nhiều config security có sẵn, JWT là một trong số đó.

Sử dụng thư viện này giúp cho bước authentication ở Security Config dễ dàng hơn, chỉ cần config và gọi thay vì viết hẳn một lớp Filter và thực hiện thủ công:

```java
public class SecurityConfig {
      @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(request -> request.requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS)
                .permitAll()
                .anyRequest()
                .authenticated());

        httpSecurity.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtConfigurer -> jwtConfigurer
                        .decoder(customJwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint()));
        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        return httpSecurity.build();
    }

        @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }
}
```

Ngoài ra, bỏ việc phân quyền ngay tại lớp SecurityConfig, giờ lớp SecurityConfig chỉ còn đảm nhận vai trò kiểm tra xem các endpoint nào là public để mà permitAll. Còn authorization cụ thể từng endpoint thì sẽ sử dụng đến hai annotation là ` @PreAuthorize("hasRole('ROLE')")` và ` @PostAuthorize("hasRole('ROLE')")`

```java
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserProfileController {
    UserProfileService userProfileService;
    @GetMapping("/{profileId}")
    UserProfileResponse getProfile(@PathVariable String profileId) {
        return userProfileService.getProfile(profileId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/")
    List<UserProfileResponse> getAllProfiles() {
        return userProfileService.getAllProfiles();
    }
}
```
Last update: 30/07/2024 