package com.example.extensivereading.service;

import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.extensivereading.dto.UserRegisterForm;
import com.example.extensivereading.entity.User;
import com.example.extensivereading.repository.UserRepository;

/**
 * ユーザー登録に関する処理のServiceクラス。
 * IDの正規化、パスワードのハッシュ化、重複チェック等の登録処理を担当する。
 */
@Service
public class UserService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * 新規ユーザーを登録
	 * @param form 入力値のバリデーションを通過したユーザー登録情報
	 * @throws IllegalArgumentException IDが既にデータベースに存在する場合に発生
	 */
	@Transactional
	public void register(UserRegisterForm form) {

		String normalizedId = form.getId().trim().toLowerCase(Locale.ROOT);

		if (userRepository.existsById(normalizedId)) {
			throw new IllegalArgumentException("このIDは既に使われています。別のIDを入力してください。");
		}

		User user = new User();
		user.setId(normalizedId);
		user.setName(form.getName().trim());

		String hashedPass = passwordEncoder.encode(form.getPassword());
		user.setPasswordHash(hashedPass);

		try {
			userRepository.save(user);
		} catch (DataIntegrityViolationException e) {
			throw new IllegalArgumentException("このIDは既に使われています。別のIDを入力してください。");
		}
	}
}
