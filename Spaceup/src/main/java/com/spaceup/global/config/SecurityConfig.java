package com.spaceup.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.spaceup.global.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable()).cors(cors -> cors.disable())
				.headers(headers -> headers.frameOptions(frame -> frame.disable()))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth.requestMatchers("/api/member/join", "/api/member/login").permitAll()
						.requestMatchers("/api/board/list/**", "/api/board/comment/list/**").permitAll()
						// ⭐ 확장 지점: 관리자 전용 API가 생기면 아래처럼 역할별로 제한하세요.
						// ⭐ 확장 지점: 다른 역할별 제한이 필요해지면 이런 식으로 추가하세요.
						// .requestMatchers("/api/quotes/**").hasAnyRole("CONTRACTOR", "LANDLORD")
						.requestMatchers("/api/admin/**").hasRole("ADMIN")
						// ⭐ 정산 생성/완료 처리는 관리자만 (조회는 본인 소유 검증을 서비스 레이어에서 별도로 함)
						.requestMatchers(HttpMethod.POST, "/api/settlements").hasRole("ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/settlements/*/complete").hasRole("ADMIN")
						.anyRequest().authenticated())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.formLogin(form -> form.disable()).httpBasic(basic -> basic.disable());

		return http.build();
	}
}