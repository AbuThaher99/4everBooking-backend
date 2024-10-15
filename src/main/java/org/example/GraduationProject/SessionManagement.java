package org.example.GraduationProject;

import org.example.GraduationProject.Common.Entities.User;
import org.example.GraduationProject.Common.Enums.Role;
import org.example.GraduationProject.WebApi.Exceptions.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionManagement {
    public void validateLoggedInAdmin(User user) throws UserNotFoundException {

        if(user.getRole() != Role.SUPER_ADMIN && user.getRole() != Role.ADMIN){
            throw new UserNotFoundException("You are not authorized to perform this operation");
        }
    }

    public void validateLoggedInCustomer(User user) throws UserNotFoundException {
        if(user.getRole() != Role.ADMIN && user.getRole() != Role.CUSTOMER){
            throw new UserNotFoundException("You are not authorized to perform this operation");
        }
    }

    public void validateLoggedInHallOwner(User user) throws UserNotFoundException {
        if(user.getRole() != Role.ADMIN && user.getRole() != Role.HALL_OWNER){
            throw new UserNotFoundException("You are not authorized to perform this operation");
        }
    }



}
