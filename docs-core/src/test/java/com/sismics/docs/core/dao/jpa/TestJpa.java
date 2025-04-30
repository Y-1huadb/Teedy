package com.sismics.docs.core.dao.jpa;

import com.sismics.docs.BaseTransactionalTest;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.util.TransactionUtil;
import com.sismics.docs.core.util.authentication.InternalAuthenticationHandler;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the persistance layer.
 * 
 * @author jtremeaux
 */
public class TestJpa extends BaseTransactionalTest {
    @Test
    public void testJpa() throws Exception {
        // Create a user
        UserDao userDao = new UserDao();
        User user = createUser("testJpa");

        TransactionUtil.commit();

        // Search a user by his ID
        user = userDao.getById(user.getId());
        Assert.assertNotNull(user);
        Assert.assertEquals("toto@docs.com", user.getEmail());

        // Authenticate using the database
        Assert.assertNotNull(new InternalAuthenticationHandler().authenticate("testJpa", "12345678"));

        // Delete the created user
        userDao.delete("testJpa", user.getId());
        TransactionUtil.commit();
    }

    @Test
    public void testUpdatePassword() throws Exception {
        // Create a user
        UserDao userDao = new UserDao();
        User user = createUser("testUpdatePassword");
        TransactionUtil.commit();
    
        // Update the user's password
        String newPassword = "newPassword123";
        user.setPassword(newPassword);
        User updatedUser = userDao.updatePassword(user, "testUserId");
        TransactionUtil.commit();
    
        // Verify the password was updated
        User fetchedUser = userDao.getById(updatedUser.getId());
        Assert.assertNotNull(fetchedUser);
        Assert.assertNotEquals("12345678", fetchedUser.getPassword()); // Ensure the password is different from the old one
    
        // Clean up: Delete the created user
        userDao.delete("testUpdatePassword", fetchedUser.getId());
        TransactionUtil.commit();
    }
}
