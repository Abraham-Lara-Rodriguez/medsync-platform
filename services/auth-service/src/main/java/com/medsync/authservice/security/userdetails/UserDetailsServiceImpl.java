package com.medsync.authservice.security.userdetails;

import com.medsync.authservice.domain.entity.User;
import com.medsync.authservice.domain.enums.UserStatus;
import com.medsync.authservice.repository.user.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Locates the user based on the email address.
     *
     * @param email the email identifying the user whose data is required.
     * @return a fully populated UserDetails object (never null)
     * @throws UsernameNotFoundException if the user could not be found
     */
    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),                         // email
                user.getPassword(),                      // password
                user.getStatus() == UserStatus.ACTIVE,   // enabled
                true,                                    // accountNonExpired
                true,                                    // credentialsNonExpired
                user.getStatus() != UserStatus.INACTIVE, // accountNonLocked
                mapAuthorities(user)
        );
    }

    /**
     * Converts user roles and permissions into GrantedAuthority collection.
     *
     * @param user the user entity
     * @return collection of granted authorities
     */
    private Collection<? extends GrantedAuthority> mapAuthorities(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        // ROLE_
        authorities.add(new SimpleGrantedAuthority(user.getRole().asAuthority()));
        // PERMISSIONS
        user.getRole().getPermissions().forEach(permission ->
                authorities.add(new SimpleGrantedAuthority(permission.name()))
        );
        return authorities;
    }
}