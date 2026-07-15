package com.spaceup.domain.member.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.spaceup.domain.member.entity.Member;

import java.util.Collection;
import java.util.List;

public class MemberPrincipal implements UserDetails {
	private final Long id;
	private final String username;
	private final String password;
	private final boolean enabled;

	public MemberPrincipal(Member member) {
		this.id = member.getId();
		this.username = member.getUsername();
		this.password = member.getPassword();
		this.enabled = !member.isWithdrawn(); // 탈퇴 회원은 비활성 계정으로 취급
	}

	public Long getId() {
		return id;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_USER"));
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
}