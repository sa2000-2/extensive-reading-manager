package com.example.extensivereading.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.extensivereading.dto.UserEditForm;
import com.example.extensivereading.entity.User;
import com.example.extensivereading.repository.BoardRepository;
import com.example.extensivereading.repository.BookRecordRepository;
import com.example.extensivereading.repository.FavoriteRepository;
import com.example.extensivereading.repository.UserRepository;

/**
 * UserEditServiceのテストクラス。
 * ユーザー情報更新、パスワード変更、入力値のバリデーション、
 * SecurityContext更新および退会処理のテストを行う。
 */
@ExtendWith(MockitoExtension.class)
class UserEditServiceTest {
	@Mock
	private UserRepository userRepository;
	
	@Mock
    private PasswordEncoder passwordEncoder;
	
	@Mock
	private BookRecordRepository bookRecordRepository;
	
	@Mock
    private BoardRepository boardRepository;
	
	@Mock
    private FavoriteRepository favoriteRepository;
	
	@Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;
	
	@InjectMocks
	private UserEditService userEditService;
	
	@BeforeEach
    void setUpSecurityContext() {
        SecurityContextHolder.setContext(securityContext);
    }
	
	
	/**
	 * updateUserメソッドにおいてユーザー情報が存在しない場合、
	 * IllegalStateExceptionが発生することを確認するテスト。
	 */
	@Test
	void updateUser_UserNotFound_ThrowIllegalStateException() {
		String userId = "none";
		UserEditForm form = new UserEditForm();
		
		when(userRepository.findById(userId)).thenReturn(Optional.empty());
		
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            userEditService.updateUser(userId, form);
        });
		
		assertEquals("ユーザー情報が存在しません", exception.getMessage());
		verify(userRepository, never()).save(any(User.class));
	}
	
	/**
	 * updateUserメソッドにおいて名前の前後に空白がある場合、空白が消されることを確認するテスト。
	 */
	@Test
	void updateUser_NameUpdated_SaveTrimmedName() {
	    String userId = "user123";

	    UserEditForm form = new UserEditForm();
	    form.setName("   田中太郎   ");

	    User existingUser = new User();
	    existingUser.setId(userId);
	    existingUser.setName("元の名前");

	    when(userRepository.findById(userId))
	            .thenReturn(Optional.of(existingUser));

	    when(securityContext.getAuthentication()).thenReturn(authentication);

	    userEditService.updateUser(userId, form);

	    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
	    verify(userRepository).save(userCaptor.capture());

	    User savedUser = userCaptor.getValue();

	    assertEquals("田中太郎", savedUser.getName());
	}
	
	
	/**
	 * updateUserメソッドにおいて入力が空白の場合元のデータが維持されることを確認するテスト。
	 */
	@Test
	void updateUser_EmptyFields_KeepOriginalValues() {
		String userId = "user123";
        UserEditForm form = new UserEditForm();
        form.setName("   ");
        form.setTargetWords(null);
        form.setNewPassword(null);
        
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("元の名前");
        existingUser.setTargetWords(10000);
        existingUser.setPasswordHash("old_hash");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        
        userEditService.updateUser(userId, form);
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
		
		User savedUser = userCaptor.getValue();
        assertEquals("元の名前", savedUser.getName());
        assertEquals(10000, savedUser.getTargetWords());
        assertEquals("old_hash", savedUser.getPasswordHash());
	}
	
	
	/**
	 * updateUserメソッドにおいてパスワードと確認用パスワードの入力が異なる場合、
	 * IllegalArgumentExceptionが発生することを確認するテスト。
	 */
	@Test
	void updateUser_PasswordsDoNotMatch_ThrowIllegalArgumentException() {
		String userId = "user123";
		UserEditForm form = new UserEditForm();
        form.setNewPassword("new123");
        form.setConfirmNewPassword("123456");
        
        User existingUser = new User();
        existingUser.setId(userId);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userEditService.updateUser(userId, form);
        });
		
		assertEquals("パスワードと確認用パスワードが一致しません。", exception.getMessage());
		verify(passwordEncoder, never()).encode(anyString());
		verify(userRepository, never()).save(any(User.class));
	}
	
	
	/**
	 * updateUserメソッドにおいて正常に名前と目標語数が変更されることを確認するテスト。
	 */
	@Test
	void updateUser_UpdateNameAndTargetWords_SaveUpdatedValues() {
		String userId = "user123";
		UserEditForm form = new UserEditForm();
		form.setName("新しい名前");
		form.setTargetWords(50000);
		
		User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("元の名前");
        existingUser.setTargetWords(10000);
		
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        
        userEditService.updateUser(userId, form);
		
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
		
        
		User savedUser = userCaptor.getValue();
		assertEquals("新しい名前", savedUser.getName());
        assertEquals(50000, savedUser.getTargetWords());
	}
	
	
	/**
	 * updateUserメソッドにおいて正常にパスワードが変更されることを確認するテスト。
	 */
	@Test
	void updateUser_ValidPasswordProvided_EncodePasswordAndUpdateSecurityContext() {
		String userId = "user123";
		UserEditForm form = new UserEditForm();
		form.setNewPassword("new123");
        form.setConfirmNewPassword("new123");
        
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setPasswordHash("old_hash");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("new123")).thenReturn("NEW_ENCRYPTED_HASH");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        
        userEditService.updateUser(userId, form);
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
		
		User savedUser = userCaptor.getValue();
		assertEquals("NEW_ENCRYPTED_HASH", savedUser.getPasswordHash());
		verify(securityContext).setAuthentication(any(Authentication.class));
	}
	
	
	/**
	 * deleteUserメソッドにおいて全てのデータが削除されることを確認するテスト。
	 */
	@Test
	void deleteUser_DeleteAllRelatedDataAndUser() {
		String userId = "user123";
		
		userEditService.deleteUser(userId);
		
		verify(bookRecordRepository).deleteByUserId(userId);
		verify(boardRepository).deleteByUserId(userId);
		verify(favoriteRepository).deleteByUserId(userId);
		verify(userRepository).deleteById(userId);
		
	}
	
	

}
