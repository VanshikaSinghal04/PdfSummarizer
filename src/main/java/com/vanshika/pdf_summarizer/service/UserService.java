package com.vanshika.pdf_summarizer.service;
import com.vanshika.pdf_summarizer.model.User;
import com.vanshika.pdf_summarizer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Service//this tells spring that this is a service class, which is a type of component that contains business logic and can be injected into other components like controllers or repositories.
public class UserService {

    @Autowired//this tell spring to create an instance of UserRepository and inject it into this service class, so we can use it to interact with the database.
    private UserRepository userRepository;

    // Save or update user when they login with Google
    public User saveOrUpdateUser(OAuth2User oauth2User) {//this method takes an OAuth2User object as a parameter, which contains information about the user who logged in with Google. It extracts the user's Google ID, name, email, and profile picture from the OAuth2User object, and then checks if the user already exists in the database by looking up their Google ID. If the user exists, it updates their name and profile picture and saves the changes to the database. If the user does not exist, it creates a new User object with the extracted information and saves it to the database. Finally, it returns the saved or updated User object.

        String googleId = oauth2User.getAttribute("sub");
        String name     = oauth2User.getAttribute("name");
        String email    = oauth2User.getAttribute("email");
        String picture  = oauth2User.getAttribute("picture");

        // Check if user already exists in database
        return userRepository.findByGoogleId(googleId)
            .map(existingUser -> {
                // User exists - update their info
                existingUser.setName(name);
                existingUser.setProfilePicture(picture);
                return userRepository.save(existingUser);
            })
            .orElseGet(() -> {
                // New user - save to database
                User newUser = new User();
                newUser.setGoogleId(googleId);
                newUser.setName(name);
                newUser.setEmail(email);
                newUser.setProfilePicture(picture);
                return userRepository.save(newUser);
            });
    }

    // Find user by Google ID
    public User findByGoogleId(String googleId) {
        return userRepository.findByGoogleId(googleId)
            .orElse(null);
    }
}