package co.hanbin.mybooks.users.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Entity
@Data
public class Users {
	
	@Id
	@Column
    private long id;

    @Column
    @NotNull(message="{NotNull.User.username}")
    private String username;
    
    @Column
    @NotNull(message="{NotNull.User.userpw}")
    private String userpw;
    
    @Column
    @NotNull(message="{NotNull.User.email}")
    private String email;

}
