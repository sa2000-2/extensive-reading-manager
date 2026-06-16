package com.example.extensivereading.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.extensivereading.dto.UserRegisterForm;
import com.example.extensivereading.entity.User;
import com.example.extensivereading.repository.UserRepository;

/**
 * UserServiceのテストクラス。
 * 正規化、パスワード暗号化、エラーメッセージ出力のテストを行う。
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
	@Mock
	private UserRepository userRepository;
	
	@Mock
	private PasswordEncoder passwordEncoder;
	
	@InjectMocks
	private UserService userService;

	/**
	 * registerメソッドにおいてIDと名前が正規化されることを確認するテスト。
	 */
	@Test
	void register_NormalizeIdAndName(){
		UserRegisterForm form = new UserRegisterForm();
		form.setId(" UseR123 ");
		form.setName(" 田中 ");
		form.setPassword("password123");
		
		when(userRepository.existsById("user123")).thenReturn(false);
		when(passwordEncoder.encode("password123")).thenReturn("hashed_abc");
		
		userService.register(form);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("user123", savedUser.getId());
        assertEquals("田中", savedUser.getName());
	}
	
	/**
	 * registerメソッドにおいてパスワードがハッシュ化された状態で保存されることを確認するテスト。
	 */
	@Test
	void register_passwordHash() {
		UserRegisterForm form = new UserRegisterForm();
		form.setId("user123");
		form.setName("田中");
		form.setPassword("password123");
		
		when(userRepository.existsById("user123")).thenReturn(false);
		when(passwordEncoder.encode("password123")).thenReturn("hashed_abc");
		
		userService.register(form);
		
		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
		
		User savedUser = userCaptor.getValue();
        assertEquals("hashed_abc", savedUser.getPasswordHash());
		
	}
	
	/**
	 * registerメソッドにおいてID重複時にIllegalArgumentExceptionが発生することを確認するテスト。
	 */
	@Test
	void register_DuplicateId_ThrowIllegalArgumentException() {
		UserRegisterForm form = new UserRegisterForm();
		form.setId("user123");
		form.setName("田中");
		form.setPassword("password123");
		
		when(userRepository.existsById("user123")).thenReturn(true);
		
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.register(form);
        });
		
		assertEquals("このIDは既に使われています。別のIDを入力してください。", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
		
	}
	
	/**
	 * registerメソッドにおいてIDの同時登録時にIllegalArgumentExceptionが発生することを確認するテスト。
	 */
	@Test
	void register_SaveConflict_ThrowIllegalArgumentException() {
		UserRegisterForm form = new UserRegisterForm();
		form.setId("user123");
		form.setName("田中");
		form.setPassword("password123");
		
		when(userRepository.existsById("user123")).thenReturn(false);
		when(passwordEncoder.encode("password123")).thenReturn("hashed_abc");
		when(userRepository.save(any(User.class))).thenThrow(DataIntegrityViolationException.class);
		
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.register(form);
        });
		
		assertEquals("このIDは既に使われています。別のIDを入力してください。", exception.getMessage());
		
	}
	
}
