package com.example.extensivereading.service;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.extensivereading.dto.UserEditForm;
import com.example.extensivereading.entity.User;
import com.example.extensivereading.repository.BoardRepository;
import com.example.extensivereading.repository.BookRecordRepository;
import com.example.extensivereading.repository.FavoriteRepository;
import com.example.extensivereading.repository.UserRepository;
import com.example.extensivereading.security.UserDetailsImpl;

/**
* ユーザー情報の更新および削除に関する業務処理を担当するServiceクラス。
* ユーザー情報の更新、パスワード更新、認証情報の更新、および退会処理を行う。
*/
@Service
public class UserEditService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	private final BookRecordRepository bookRecordRepository;
	private final BoardRepository boardRepository;
	private final FavoriteRepository favoriteRepository;

	public UserEditService(UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			BookRecordRepository bookRecordRepository,
			BoardRepository boardRepository,
			FavoriteRepository favoriteRepository) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.bookRecordRepository = bookRecordRepository;
		this.boardRepository = boardRepository;
		this.favoriteRepository = favoriteRepository;
	}

	/**
	 * ユーザー情報を更新し、変更内容を認証情報へ反映する。
	 * @param userId ユーザーID
	 * @param form 編集画面から入力された変更情報
	 * @throws IllegalStateException 該当するユーザー情報が存在しない場合
	 * @throws IllegalArgumentException 新しいパスワードと確認パスワードが一致しない場合
	 */
	@Transactional
	public void updateUser(String userId, UserEditForm form) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalStateException("ユーザー情報が存在しません"));

		if (form.getName() != null && !form.getName().trim().isEmpty()) {
			user.setName(form.getName().trim());
		}

		if (form.getTargetWords() != null) {
			user.setTargetWords(form.getTargetWords());
		}

		String newPass = form.getNewPassword();
		String confirmPass = form.getConfirmNewPassword();

		if (newPass != null && !newPass.isEmpty()) {
			if (!newPass.equals(confirmPass)) {
				throw new IllegalArgumentException("パスワードと確認用パスワードが一致しません。");
			}
			user.setPasswordHash(passwordEncoder.encode(newPass));
		}

		userRepository.save(user);

		Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
		UserDetailsImpl updatedUserDetails = new UserDetailsImpl(user);
		Authentication newAuth = new UsernamePasswordAuthenticationToken(
				updatedUserDetails,
				currentAuth.getCredentials(),
				currentAuth.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(newAuth);
	}

	/**
	 * 指定されたユーザーに関連する読書記録・掲示板・お気に入り情報を削除した後、ユーザー情報を削除する。
	 * @param userId 削除対象のユーザーID
	 */
	@Transactional
	public void deleteUser(String userId) {
		bookRecordRepository.deleteByUserId(userId);
		boardRepository.deleteByUserId(userId);
		favoriteRepository.deleteByUserId(userId);

		userRepository.deleteById(userId);
	}

}
