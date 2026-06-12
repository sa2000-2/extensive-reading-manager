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
     * 権限者リストを返す
     * @return 権限者リスト（ユーザー登録した人全員）
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.createAuthorityList("ROLE_USER");
    }

    /**
     * 今回は未使用の為すべて有効にする
     * @return　true
     */
    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }

    /**
     * ユーザー名を返す。
     * @return ユーザー名
     */
    public String getName() {
        return this.user.getName();
    }
}

