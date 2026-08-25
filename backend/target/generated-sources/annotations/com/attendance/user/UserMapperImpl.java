package com.attendance.user;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-25T07:39:24+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDto toDto(User user) {
        if ( user == null ) {
            return null;
        }

        Long id = null;
        String username = null;
        String fullName = null;
        Role role = null;
        boolean active = false;

        id = user.getId();
        username = user.getUsername();
        fullName = user.getFullName();
        role = user.getRole();
        active = user.isActive();

        UserDto userDto = new UserDto( id, username, fullName, role, active );

        return userDto;
    }
}
