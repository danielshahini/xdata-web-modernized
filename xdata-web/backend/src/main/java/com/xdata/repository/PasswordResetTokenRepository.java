package com.xdata.repository;

import com.xdata.model.PasswordResetToken;
import com.xdata.model.XDataUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUser(XDataUser user);
}
