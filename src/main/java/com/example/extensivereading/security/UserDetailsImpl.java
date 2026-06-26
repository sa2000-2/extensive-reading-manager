package com.example.extensivereading.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.extensivereading.entity.User;

/**
 * Spring Securityの認証用ユーザー情報クラス。
 * Userエンティティを Spring Security が扱える形式に変換する。
 */
public class UserDetailsImpl implements UserDetails {

	/**
	 * Userエンティティをもとに認証用ユーザー情報を生成する。
	 * @param user 認証対象のユーザー情報
	 */
	private final User user;

	public UserDetailsImpl(User user) {
		this.user = user;
	}

	/**
	* ログインIDを返す。
	* @return ユーザーID
	*/
	@Override
	public String getUsername() {
		return user.getId();
	}

	/**
	* パスワードを返す。
	* @return ハッシュ化済みパスワード
	*/
	@Override
	public String getPassword() {
		return user.getPasswordHash();
	}

	/**
	 * ユーザーに付与する権限を返す。
	 * @return 権限情報（ROLE_USER）
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return AuthorityUtils.createAuthorityList("ROLE_USER");
	}

	/**
	 * アカウントの有効状態を返す。
	 * 期限切れ・ロック・無効化を管理しないため、常にtrueを返す。
	 * @return 常にtrue
	 */
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
		return true;
	}

	/**
	 * ユーザー名を返す。
	 * @return ユーザー名
	 */
	public String getName() {
		return this.user.getName();
	}
}
