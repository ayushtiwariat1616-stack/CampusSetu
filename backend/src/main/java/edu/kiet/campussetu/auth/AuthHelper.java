package edu.kiet.campussetu.auth;

import edu.kiet.campussetu.common.ResourceNotFoundException;
import edu.kiet.campussetu.user.OnboardingStatus;
import edu.kiet.campussetu.user.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class AuthHelper {

    public static final String SESSION_USER_KEY = "CAMPUSSETU_SESSION_USER";

    public SessionUser getSessionUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        Object obj = session.getAttribute(SESSION_USER_KEY);
        if (obj instanceof SessionUser su) {
            return su;
        }
        return null;
    }

    public SessionUser requireUser(HttpServletRequest request) {
        SessionUser user = getSessionUser(request);
        if (user == null) {
            throw new AccessDeniedException("Authentication required. Please sign in with your college account.");
        }
        return user;
    }

    public SessionUser requireApprovedUser(HttpServletRequest request) {
        SessionUser user = requireUser(request);
        if (user.getOnboardingStatus() != OnboardingStatus.APPROVED) {
            throw new AccessDeniedException("Your account is pending roster verification by college administration.");
        }
        return user;
    }

    public SessionUser requireStudent(HttpServletRequest request) {
        SessionUser user = requireApprovedUser(request);
        if (user.getRole() != UserRole.STUDENT) {
            throw new AccessDeniedException("This action is restricted to verified student accounts.");
        }
        return user;
    }

    public SessionUser requireVerifiedOperator(HttpServletRequest request) {
        SessionUser user = requireApprovedUser(request);
        if (!user.isOperator() && !user.isSystemAdmin()) {
            throw new AccessDeniedException("Operator privileges are required.");
        }
        if (!user.isOperatorKeyVerified()) {
            throw new AccessDeniedException("Operator key verification is required to access this service.");
        }
        return user;
    }

    public SessionUser requireSystemAdmin(HttpServletRequest request) {
        SessionUser user = requireApprovedUser(request);
        if (!user.isSystemAdmin()) {
            throw new AccessDeniedException("System Administrator privileges are required.");
        }
        return user;
    }

    public void setSessionUser(HttpServletRequest request, SessionUser user) {
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_USER_KEY, user);
    }

    public void invalidate(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
