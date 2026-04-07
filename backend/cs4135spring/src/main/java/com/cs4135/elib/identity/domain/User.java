@Entity
public class User {
    @Id
    @GeneratedValue

    private Long id;
    private String username;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    //I am getting and setting all!
}
