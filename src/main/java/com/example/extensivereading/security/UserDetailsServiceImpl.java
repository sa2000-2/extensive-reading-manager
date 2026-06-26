package com.example.extensivereading.security;

import java.util.Locale;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.extensivereading.entity.User;
import com.example.extensivereading.repository.UserRepository;

/**
 * Spring Securityの認証処理で使用するユーザー情報を提供するServiceクラス。
 * データベースからユーザー情報を取得し、認証用のUserDetailsへ変換する。
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserRepository userRepository;

	public UserDetailsServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	/**
	 * ログインIDでユーザー情報を検索し、認証用のUserDetailsを生成する。
	 * @param username ログイン画面で入力されたユーザーID
	 * @return UserDetailImpl 認証に使用するUserDetailsオブジェクト
	 * @throws UsernameNotFoundException データベースに該当するユーザーが存在しない場合に発生
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		String normalizedUsername = username.trim().toLowerCase(Locale.ROOT);

		User user = userRepository.findById(normalizedUsername)
				.orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: " + username));
		return new UserDetailsImpl(user);
	}
}