package com.drdl.plm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.drdl.plm.Repository.ProjectRepository;
import com.drdl.plm.Repository.user_projectsRepository;
import com.drdl.plm.dao.UserDao;
import com.drdl.plm.entity.Customers;
import com.drdl.plm.entity.Organisation;
import com.drdl.plm.entity.Part;
import com.drdl.plm.entity.Project;
import com.drdl.plm.entity.User;
import com.drdl.plm.entity.User_Role;
import com.drdl.plm.entity.Vendors;
import com.drdl.plm.entity.user_projects;
import com.drdl.plm.exception.DisabledUserException;
import com.drdl.plm.exception.InvalidUserCredentialsException;
import com.drdl.plm.exception.ResourceNotFound;
import com.drdl.plm.response.Request;
import com.drdl.plm.response.Respone;
import com.drdl.plm.service.UserService;
import com.drdl.plm.service.globalfilepathService;
import com.drdl.plm.util.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javassist.expr.Instanceof;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequestMapping("/user")
@CrossOrigin("*")
public class UserController {

	private static String UPLOADED_FOLDER = "C:\\plm\\Server-Overall\\Server-Overall\\front\\src\\assets\\photos\\";

	@Autowired
	private globalfilepathService globalfilepathservice;

	@Autowired
	private UserService userService;

	@Autowired
	private UserDao userdao;

	@Autowired
	private ProjectRepository projrepo;

	@Autowired
	private user_projectsRepository userprojrepo;

// @Autowired
//    private UserroleRepository userroleRepository;
//    
	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private user_projectsRepository usrproject;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private PasswordEncoder encoder;

//    @PostConstruct
//    public void initRoleAndUser() throws Exception {
//        userService.initRoleAndUser();
//    }

//    @PostMapping({"/registerNewUser"})
//    public User registerNewUser(@RequestBody User user) throws Exception {
//        return userService.registerNewUser(user);
//    }

	@PostMapping("/")
	public User addUser(@RequestBody User user, HttpServletRequest httpServletRequest) {
		String userip = httpServletRequest.getRemoteAddr();
		user.setUserIp(userip);
		return this.userService.addUser(user);
	}

	@PutMapping("/")
	public User updateBy(@RequestBody User user) {
		User user1 = userdao.findByuserName(user.getUserName())
				.orElseThrow(() -> new ResourceNotFound("uaser not exist with username :" + user.getUserName()));
		;
		user1.setUserPassword(user.getUserPassword());
		User updateduser = userdao.save(user1);

		return updateduser;
	}

	@GetMapping({ "/forAdmin" })
	@PreAuthorize("hasRole('Admin')")
	public String forAdmin() {
		return "This URL is only accessible to the admin";
	}

	@GetMapping({ "/forUser" })
	@PreAuthorize("hasRole('User')")
	public String forUser() {
		return "This URL is only accessible to the user";
	}
//    //http://localhost:8080/user/project?userName=920029  Request Param
//    @GetMapping("/project")
//    public Set<Project> allProjects(@RequestParam("userName") String user_name){
//    	Set<Project> proj=projrepo.allProjects(user_name);
//    	System.out.println(proj+"manyyyyyyyyyyyyyyyyyyyyy");
//    	return proj;
//    }
//    

	// http://localhost:8080/user/project?userName=920029 Request Param
	@GetMapping("/project")
	public Set<user_projects> allProjects(@RequestParam("userName") String user_name) {
		Set<user_projects> userproj = usrproject.allProjects(user_name);
		System.out.println(userproj + "manyyyyyyyyyyyyyyyyyyyyy");
		return userproj;
	}

//    @GetMapping("/countOfActiveUsers")
//   public Integer countOfActiveUsers() {
//    	Integer countOfActiveUsers = userdao.countOfActiveUsers();
//    	System.err.println("countOfActiveUsers-----"+countOfActiveUsers);
//	  return  countOfActiveUsers;
//   }
//	

	@GetMapping("/countOfTotalUsers")
	public Map<String, Long> countOfTotalUsers() {
		List<Object[]> countOfUsers = userdao.countOfUsers();

		Map<String, Long> resultMap = new HashMap<>();

		if (!countOfUsers.isEmpty()) {
			Object[] result = countOfUsers.get(0);

			long inActiveUsersValue = ((Number) result[0]).longValue();
			long activeUsersValue = ((Number) result[1]).longValue();
			long DeactivatedUsersValue = ((Number) result[2]).longValue();
			long OtherUsersValue = ((Number) result[3]).longValue();
			long TotalUsersValue = ((Number) result[4]).longValue();
			resultMap.put("inActiveUsers", inActiveUsersValue);

			resultMap.put("ActiveUsers", activeUsersValue);

			resultMap.put("DeactivatedUsers", DeactivatedUsersValue);

			resultMap.put("OtherUsers", OtherUsersValue);

			resultMap.put("TotalUsers", TotalUsersValue);

		} else {
			System.out.println("no data");
		}

		return resultMap;

	}
	
	
	  @GetMapping("/DashBoard") 
	  public Map<String, Long>llDashBoardCount(@RequestParam("ProjName") String ProjName,@RequestParam("UserPersno")String UserPersno) 
	  {
		  List<Object[]>allDashBoardCount = userdao.AllDashBoardCounts(ProjName,UserPersno);
	  
	  
	Map<Object, Object> collect = allDashBoardCount.stream().collect(Collectors.toMap(parts->parts[1],parts->parts[0]));
	  System.err.println("collect-----------------------------------"+collect.toString());
	  Map<String, Long> resultMap = new HashMap<>();
	  if (!allDashBoardCount.isEmpty())
	  {
		  Object[] result = allDashBoardCount.get(0);
	  System.err.println("result-----------"+result.toString()); 
	  long getCompletedMeetingsCount = ((Number) result[0]).longValue();
	  System.err.println("getCompletedMeetingsCount---------------"+
	  getCompletedMeetingsCount);
	  long getPendingMeetingsCount = ((Number)result[1]).longValue(); 
	  long message_count_UNSEEN = ((Number)result[2]).longValue(); 
	  long message_count_SEEN = ((Number)result[3]).longValue(); 
	  long dak_open_count = ((Number)result[4]).longValue();
	  
	  long dak_closed_count = ((Number) result[5]).longValue(); 
	  long Noti_Unseen_Count = ((Number) result[6]).longValue(); 
	  long Noti_seen_Count =((Number) result[7]).longValue(); 
	  long task_pend_Count = ((Number) result[8]).longValue();
	  long task_completed_Count = ((Number)result[9]).longValue();
	  
	  
	  resultMap.put("getCompletedMeetingsCount", getCompletedMeetingsCount);
	  
	  resultMap.put("getPendingMeetingsCount", getPendingMeetingsCount);
	  
	  resultMap.put("message_count_UNSEEN", message_count_UNSEEN);
	  
	  resultMap.put("message_count_SEEN", message_count_SEEN);
	  
	  resultMap.put("dak_open_count", dak_open_count);
	  
	  resultMap.put("dak_closed_count", dak_closed_count);
	  
	  resultMap.put("Noti_Unseen_Count", Noti_Unseen_Count);
	  
	  resultMap.put("Noti_seen_Count", Noti_seen_Count);
	  
	  resultMap.put("task_pend_Count", task_pend_Count);
	  
	  resultMap.put("task_completed_Count", task_completed_Count);
	  
	  
	  } else { System.out.println("no data"); }
	  
	  
	  return resultMap;
	  
	  }
	 
	
	
	
	
	
	
	
	@GetMapping("/Dash")
	public  String AllDashBoardCounts(@RequestParam("ProjName") String ProjName,@RequestParam("UserPersno")String UserPersno) {
//		http://10.66.40.212:8080/user/Dash?ProjName=DIT&UserPersno=920029
		String json=null;
		List<Map<String, Object>> allDashBoardCount = userdao.AllDashBoardCount(ProjName,UserPersno);
		
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			json = objectMapper.writeValueAsString(allDashBoardCount);
			System.err.println("jsonnnnnn----" + json);
		} catch (Exception e) {
			// TODO: handle exception
			e.getMessage();
		}
return json;

	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	@GetMapping("/countOfUsersPerMonth")
	public String countOfUsersPerMonth() {

		List<Object[]> countOfUsersPerMonth = userdao.countOfUsersPerMonth();
		List<Map<String, Object>> result = new ArrayList<>();
		String json = null;
		for (Object[] objArray : countOfUsersPerMonth) {

			Map<String, Object> map = new HashMap<>();
			map.put("year_month", objArray[0]);
			map.put("users_count", objArray[1]);
			result.add(map);

			try {
				ObjectMapper objectMapper = new ObjectMapper();
				json = objectMapper.writeValueAsString(result);
				System.err.println("jsonnnnnn----" + json);
			} catch (Exception e) {
				// TODO: handle exception
				e.getMessage();
			}

		}

		return json;
	}

	@GetMapping("/ListOfUserLoggedIn")
	public String ListOfUserLoggedIn() {
		List<Object[]> userLoggedIn = userdao.listOfUserLoggedIn();
		List<Map<String, Object>> result = new ArrayList<>();
		Map<String, Object> collect = userLoggedIn.stream()
		.collect(Collectors.toMap(data->(String)data[0], data->data[1]));
		
		
		System.err.println("coleect--------------------"+collect.toString());
		String json = null;
		for (Object[] objArray : userLoggedIn) {

			Map<String, Object> map = new HashMap<>();
			map.put("Project Name", objArray[0]);
			map.put("First Name", objArray[1]);
			map.put("Last Name", objArray[2]);
			map.put("User Name", objArray[3]);
			map.put("Designation", objArray[4]);
			result.add(map);

			try {
				ObjectMapper objectMapper = new ObjectMapper();
				json = objectMapper.writeValueAsString(result);

			} catch (Exception e) {
				// TODO: handle exception
				e.getMessage();
			}
		}
		return json;

	}

	@GetMapping("/countOfUsersPerProjects")
	public String countOfUsersPerProjects() throws JsonProcessingException {
		List<Object[]> countOfUsersPerProjects = userdao.countOfUsersPerProjects();

		System.err.println("countOfUsersPerProjects----------" + countOfUsersPerProjects);
		List<Map<String, Object>> result = new ArrayList<>();

		String json = null;

		for (Object[] objArray : countOfUsersPerProjects) {

			Map<String, Object> map = new HashMap<>();
			map.put("inActiveUsers", objArray[0]);
			map.put("ActiveUsers", objArray[1]);
			map.put("DeActivatedUsers", objArray[2]);
			map.put("OtherUsers", objArray[3]);
			map.put("TotalUsers", objArray[4]);
			map.put("ProjectName", objArray[5]);
			result.add(map);

			try {
				ObjectMapper objectMapper = new ObjectMapper();
				json = objectMapper.writeValueAsString(result);
				System.err.println("jsonnnnnn----" + json);
			} catch (Exception e) {
				// TODO: handle exception
				e.getMessage();
			}

		}

		return json;
	}
	
	

	@GetMapping("/listOfUsersPerProjects")
	public List<Map<String, Object>> listOfUsersPerProjects(@RequestParam("ProjName") String ProjName) throws JsonProcessingException {
		
		List<Map<String, Object>> listOfUsersPerProjects = userdao.listOfUsersPerProjects(ProjName);
		return listOfUsersPerProjects;
	}
	
	
	@GetMapping("/{userName}")
	public User getUserById(@PathVariable String userName) {
		User user = userdao.getUserById(userName);
		return user;
	}
//   
// 

	// to know who was logged in
	@GetMapping("/current-user")
	public User getCurrentUser(Principal principal) {
		System.out.println(userdao.getUser(principal.getName()) + "principallllllllllllllllllllllllllllllllll");
		return this.userdao.getUser(principal.getName());

	}

	// user will give username or pisno to login
	@GetMapping("/getUser/{userName}")
	public User getUser(@PathVariable String userName) {
		return this.userdao.getUser(userName);

	}

	// @PutMapping("/changepassword")
	// public User updateBy(@RequestParam("userPassword")String
	// oldpassword,@RequestParam("newPassword")String
	// newPassword,@RequestParam("userName") String
	// username,@RequestParam("resetstatus") String resetstatus,@RequestBody User
	// currentuser) {

	// User user=userdao.findByuserName(username).orElseThrow(()-> new
	// ResourceNotFound("Not Found"));

	// // User user = userdao.findByUserPassword(oldpassword).orElseThrow(()-> new
	// ResourceNotFound("Not Found"));
	// if(this.encoder.matches (oldpassword,user.getUserPassword())) {
	// System.err.println("uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu");
	// user.setUserPassword(getEncodedPassword(newPassword));
	// }
	// user.setResetstatus(resetstatus);
	// User updateduser = userdao.save(user);
	// System.out.println("updateduser-----------"+updateduser);
	// return updateduser;
	// }

	// @PutMapping("/adminresetpass/")
	// public User updateBy1(@RequestBody User user) {
	// User user1 = userdao.findByuserName(user.getUserName())
	// .orElseThrow(() -> new ResourceNotFound("uaser not exist with username :" +
	// user.getUserName()));
	// user1.setUserPassword(getEncodedPassword(user.getUserPassword()));

	// user1.setResetstatus(user.getResetstatus());
	// User updateduser = userdao.save(user1);
	// return updateduser;
	// }

	@PutMapping("/changepassword")
	public ResponseEntity<User> updateBy(@RequestParam("userPassword") String oldpassword,
			@RequestParam("newPassword") String newPassword, @RequestParam("userName") String username,
			@RequestParam("resetstatus") String resetstatus, @RequestBody User currentuser) throws Exception {

		User user = userdao.findByuserName(username).orElseThrow(() -> new ResourceNotFound("Not Found"));

		// User user = userdao.findByUserPassword(oldpassword).orElseThrow(()-> new
		// ResourceNotFound("Not Found"));
//	   	     if(this.encoder.matches (oldpassword,user.getUserPassword())) {

		System.err.println(
				this.encoder.matches(oldpassword, user.getUserPassword()) + "hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");

//		try {

		if (this.encoder.matches(oldpassword, user.getUserPassword())) {
			System.err.println("oldpassword user entered===" + oldpassword);
			user.setUserPassword(getEncodedPassword(newPassword));
			user.setTimestamp(new Date());
			user.setResetstatus(resetstatus);
			User updateduser = userdao.save(user);
//	 		return "Changed";
			return new ResponseEntity<>(updateduser, HttpStatus.OK);
		}
//		}catch (BadCredentialsException e) {
//            throw  new InvalidUserCredentialsException("INVALID_CREDENTIALS");
//        }

		else {
//		return "Error";
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
//		System.out.println("updateduser-----------"+updateduser);

	}

	// @PutMapping("/adminresetpass/")
	// public User updateBy1(@RequestBody User user) {
	// User user1 = userdao.findByuserName(user.getUserName())
	// .orElseThrow(() -> new ResourceNotFound("uaser not exist with username :" +
	// user.getUserName()));
	// user1.setUserPassword(getEncodedPassword(user.getUserPassword()));

	// user1.setResetstatus(user.getResetstatus());
	// User updateduser = userdao.save(user1);
	// return updateduser;
	// }

//	@PutMapping("/adminresetpass/")
//	public User updateBy1(@RequestBody User user) {
//		User user1 = userdao.findByuserNameOrPersno(user.getUserName(),user.getPersno())
//				.orElseThrow(() -> new ResourceNotFound("uaser not exist with username :" + user.getUserName()));
//		user1.setUserPassword(getEncodedPassword(user.getUserPassword()));
//	
//		user1.setResetstatus(user.getResetstatus());
//		User updateduser = userdao.save(user1);
//		return updateduser;
//	}

	@PutMapping("/adminresetpass/")
	public User updateBy1(@RequestBody User user) {
		User user1 = userdao.findByuserNameOrPersno(user.getUserName(), user.getPersno())
				.orElseThrow(() -> new ResourceNotFound("uaser not exist with username :" + user.getUserName()));
		user1.setUserPassword(getEncodedPassword(user.getUserPassword()));

		user1.setResetstatus(user.getResetstatus());
		User updateduser = userdao.save(user1);
		return updateduser;
	}

	@GetMapping("/proj/{fprojectname}")
	public List<User> getUserByProjectName(@PathVariable String fprojectname) {
		List<Project> findByFprojectname = projrepo.findByFprojectname(fprojectname);

		List<User> findAllUsers = (List<User>) userdao.findAll();
		List<User> collectAllUsersWithProjectname = null;
		for (Project p : findByFprojectname) {
			collectAllUsersWithProjectname = findAllUsers.stream()
					.filter(u ->u.getStatus()!=null && u.getProject().contains(p) & u.getStatus().equals("Y")).collect(Collectors.toList());

		}
		System.out.println("collectAllUsersWithProjectname--------------" + collectAllUsersWithProjectname);
		return collectAllUsersWithProjectname;
	}

	@GetMapping("/project/{fprojectname}")
	public String getUserByActive(@PathVariable String fprojectname) {
		List<Project> findByFprojectname = projrepo.findByFprojectname(fprojectname);
        String json=null;
		List<User> findAllUsers = (List<User>) userdao.findAll();
		List<User> collectAllUsersWithProjectnameandActive = null;
		for (Project p : findByFprojectname) {
			collectAllUsersWithProjectnameandActive = findAllUsers.stream()
					.filter(u -> u.getProject().contains(p) && u.isActive() && u.getStatus().equals("Y"))
					.collect(Collectors.toList());

		}
		System.out.println("collectAllUsersWithProjectname--------------" + collectAllUsersWithProjectnameandActive);
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			json = objectMapper.writeValueAsString(collectAllUsersWithProjectnameandActive);
			System.err.println("jsonnnnnn----" + json);
		} catch (Exception e) {
			// TODO: handle exception
			e.getMessage();
		}
		return json;
	}

	private String getEncodedPassword(String userPassword) {
		// TODO Auto-generated method stub
		return passwordEncoder.encode(userPassword);
	}

	@PostMapping("/globalfilepath")
	public void sendglobalfilepath(@RequestBody String projectName) {

		globalfilepathservice.assignpath(projectName);

	}

	@GetMapping("/getUser")
	public List<User> getUser() {
		return this.userService.getUser();

	}

	@GetMapping("/getAllUsers")
	public List<User> getAllUsers() {
		return this.userService.getAllUsers();

	}

	@GetMapping("/getProjects/{projectname}")
	public List<User> getProjectsWithUsers(@PathVariable String projectname) {

		return this.userService.getProjectswithUsers(projectname);

	}

	@PostMapping(value = "/imgupload/{id}", consumes = MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<User> handleimgupload(@RequestPart("file") MultipartFile file, @PathVariable String id)
			throws IOException {
		User user = userdao.findByuserName(id)
				.orElseThrow(() -> new ResourceNotFound("Part not exist with PartNumber :" + id));
		try {
			final Path path = Paths.get(UPLOADED_FOLDER);
			if (!Files.exists(path)) {
				Files.createDirectories(path);
			}

//			Path filepath= path.resolve(file.getOriginalFilename());
			Path filepath = path.resolve(id + ".jpg");
			Files.copy(file.getInputStream(), filepath, StandardCopyOption.REPLACE_EXISTING);
			user.setImgname(id + ".jpg");
//		user.setImgname(file.getOriginalFilename());
			user.setImguri(filepath.toString() + ".jpg");
			User updateduser = userdao.save(user);
			return ResponseEntity.ok(updateduser);

		} catch (IOException e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@PutMapping("/{id}")
	public User updateUser(@RequestBody User user, @PathVariable("id") String id) {

		return userService.updateUser(user, id);

	}

	// @PostMapping(value="/imgupload/{id}", consumes=MULTIPART_FORM_DATA_VALUE)
	// public ResponseEntity<User> handleimgupload(@RequestPart("file")
	// MultipartFile file,@PathVariable String id) throws IOException{
	// User user = userdao.findByuserName(id).orElseThrow(() -> new
	// ResourceNotFound("Part not exist with PartNumber :" + id));
	// try {
	// final Path path=Paths.get(UPLOADED_FOLDER);
	// if(!Files.exists(path)) {
	// Files.createDirectories(path);
	// }

	// Path filepath= path.resolve(file.getOriginalFilename());

	// Files.copy(file.getInputStream(),filepath,StandardCopyOption.REPLACE_EXISTING);

	// user.setImgname(file.getOriginalFilename());
	// user.setImguri(filepath.toString());
	// User updateduser=userdao.save(user);
	// return ResponseEntity.ok(updateduser) ;

	// }
	// catch(IOException e)
	// {
	// return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	// }

	// }

	// @PostMapping("/userbulk")
	// public User[] Add_bulk_org(@RequestBody User[] user){// this method is used
	// to store data into database(part entity)

	// for(int i=0;i<user.length;i++)
	// {

	// userdao.save(user[i]);
	// }
	// //return user_repo.saveAll(part);
	// return user;
	// }

	@PostMapping("/userbulk")
	public User[] Add_bulk_org(@RequestBody User[] user, HttpServletRequest httpServletRequest) {
		String userip = httpServletRequest.getRemoteAddr();// this method is used to store data into database(part
															// entity)

		for (int i = 0; i < user.length; i++) {
			user[i].setUserIp(userip);
			user[i].setTimestamp(new Date());

			user[i].setUserPassword(getEncodedPassword(user[i].getUserPassword()));
//            user[i].setUserPassword(getEncodedPassword());
			userdao.save(user[i]);
		}
		// return user_repo.saveAll(part);
		return user;
	}

	// new added
// 	@PostMapping("/projects")
// 	public user_projects[] addUserProjects(@RequestBody user_projects[] userprojects){// this method is used to store data into database(part entity)

// 		for(int i=0;i<userprojects.length;i++)
// 		{
// //System.out.print(userprojects[i]+"aaaaaaaaaaaaaaaaaaaaa");
// 		userprojrepo.save(userprojects[i]);
//     	}
// 		//return user_repo.saveAll(part);
// 		return userprojects;
// 	}

	@PostMapping("/projects")
	public user_projects addUserProjects(@RequestBody user_projects userprojects) {// this method is used to store data
																					// into database(part entity)

//System.out.print(userprojects[i]+"aaaaaaaaaaaaaaaaaaaaa");
		return userprojrepo.save(userprojects);

		// return user_repo.saveAll(part);

	}

	@PostMapping("/userprojectbulk")
	public user_projects[] Add_bulk_userproject(@RequestBody user_projects[] userproject) {// this method is used to
																							// store data into
																							// database(part entity)

		for (int i = 0; i < userproject.length; i++) {

			userprojrepo.save(userproject[i]);
		}
		// return user_repo.saveAll(part);
		return userproject;
	}

	@PutMapping("/delete/{id}")
	public User updateUserdelete(@PathVariable String id) {
		return this.userService.updateDetailsofdelete(id);
	}

	@GetMapping("/status")
	public List<User> getbystatususer(String sid) {
		return userdao.getbyYvalus(sid);
	}

	@GetMapping("/getuserproject")
	public List<user_projects> getuserproject() {
		return this.userService.getUser_projects();
	}

	// @GetMapping("/statusproj")
	// public List<user_projects> getbystatususerproj(String sid){
	// return userprojrepo.getbyYvaluss(sid);
	// }

	@PutMapping("/deleteproj/{id}/{id1}")
	public int updateDetailsofuserproject(@PathVariable String id, @PathVariable String id1) {
		int i = this.userprojrepo.statusChange(id, id1);
		return i;
	}

	@GetMapping("/statusproj")
	public List<user_projects> getbystatususerproj(String sids) {
		return userprojrepo.getbyproject(sids);
	}

	@GetMapping("/active")
	public ResponseEntity<List<User>> getActiveUsers() {
		List<User> activeUsers = userService.getActiveUsers();
		return ResponseEntity.ok(activeUsers);
	}

}
