package response.dto;

import request.DtoMap;

public class UserProfileDto extends DtoBase{
    @DtoMap(taskName="User")
    public UserDto userDto;
}
