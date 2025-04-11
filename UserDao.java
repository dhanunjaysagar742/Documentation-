package com.drdl.plm.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.drdl.plm.Repository.ProjectRepository;
import com.drdl.plm.entity.Customers;
import com.drdl.plm.entity.Project;
import com.drdl.plm.entity.User;

import java.util.Optional;

@Repository
public interface UserDao extends CrudRepository<User, String> {

	@Query(value = "select * from t_user where f_user_name=?1 or f_persno=?1  ", nativeQuery = true)
	public User getUser(String userName);

	@Query(value = "select SUM(CASE WHEN f_active=0 AND f_delete_flag='Y' THEN 1 ELSE 0 END)AS inActiveUsers,\n"
			+ "     SUM(CASE WHEN f_active=1 AND f_delete_flag='Y' THEN 1 ELSE 0 END)AS activeUsers,\n"
			+ "     SUM(CASE WHEN f_active=0 AND f_delete_flag='N' THEN 1 ELSE 0 END)AS DeactivatedUsers,\n"
			+ "     SUM(CASE WHEN f_delete_flag IS null  THEN 1 ELSE 0 END)AS OtherUsers,\n"
			+ "     SUM(CASE WHEN f_delete_flag IN('Y','N') OR f_delete_flag IS null  THEN 1 ELSE 0 END)AS TotalUsers\n"
			+ "     from t_user", nativeQuery = true)
	public List<Object[]> countOfUsers();

	@Query(value = "SELECT \n"
			+ "    DISTINCT(select count(*) from t_meeting where    f_user_project=?1  and F_MEET_STATUS='COMPLETED' and f_delete_flag='Y'  and  f_meet_id  in (SELECT  f_meet_id FROM t_meet_participants where t_meet_participants.select_participants=?2)) AS getCompletedMeetingsCount,\n"
			+ "(select count(*) from t_meeting where    f_user_project=?1  and F_MEET_STATUS='PENDING' and f_delete_flag='Y'  and  f_meet_id  in (SELECT f_meet_id FROM t_meet_participants where t_meet_participants.select_participants=?2))   AS getPendingMeetingsCount,\n"
			+ " (SELECT COUNT(*) FROM t_message WHERE f_delete_flag = 'Y' AND f_message_status = 'UNSEEN' and f_user_project=?1 and f_message_receiver_persno=?2)  AS message_count_UNSEEN,\n"
			+ "(SELECT COUNT(*) FROM t_message WHERE f_delete_flag = 'Y' AND f_message_status = 'SEEN' and f_user_project=?1 and f_message_receiver_persno=?2)   AS message_count_SEEN,   \n"
			+ " (SELECT COUNT(*) FROM t_dak WHERE F_DAK_MARKED_TO_PERSNO=?2 and  f_dak_status='OPEN' and f_delete_flag='Y' and f_user_project=?1 )   AS dak_open_count    ,\n"
			+ "  (SELECT COUNT(*) FROM t_dak WHERE F_DAK_MARKED_TO_PERSNO=?2 and  f_dak_status='CLOSE' and f_delete_flag='Y' and f_user_project=?1 )   AS dak_closed_count    ,\n"
			+ "(SELECT COUNT(*) FROM t_notifications WHERE  F_NOTE_PROJECT=?1 and F_NOTE_ACK_STATUS='UNSEEN')   AS Noti_Unseen_Count ,\n"
			+ "(SELECT COUNT(*) FROM t_notifications WHERE  F_NOTE_PROJECT=?1 and F_NOTE_ACK_STATUS='SEEN')   AS Noti_seen_Count ,\n"
			+ "(SELECT COUNT(*) FROM t_task WHERE  F_TASK_ASSIGNED_TO_PERSNO=?2  and f_user_project=?1 and F_TASK_STATUS not in ('100') and f_delete_flag='Y')   AS  task_pend_Count ,\n"
			+ "(SELECT COUNT(*) FROM t_task WHERE  F_TASK_ASSIGNED_TO_PERSNO=?2  and f_user_project=?1 and F_TASK_STATUS='100' and f_delete_flag='Y')   AS  task_completed_Count       \n"
			+ "FROM\n" + "    t_dak d,\n" + "    t_meeting m ,\n" + "    t_message a ,t_notifications n ,t_task t\n"
			+ "where\n" + "    m.f_part_name = a.f_part_name and\n" + "     a.f_part_name = n.f_note_part_name and \n"
			+ "     d.f_part_name = m.f_part_name and\n" + "     m.f_part_name=t.f_part_name", nativeQuery = true)
	public List<Map<String, Object>> AllDashBoardCount(String ProjName,String UserPersno);
	
	
	@Query(value = "SELECT \n"
			+ "    DISTINCT(select count(*) from t_meeting where    f_user_project=?1  and F_MEET_STATUS='COMPLETED' and f_delete_flag='Y'  and  f_meet_id  in (SELECT  f_meet_id FROM t_meet_participants where t_meet_participants.select_participants=?2))  AS getCompletedMeetingsCount,\n"
			+ "(select count(*) from t_meeting where    f_user_project=?1  and F_MEET_STATUS='PENDING' and f_delete_flag='Y'  and  f_meet_id  in (SELECT f_meet_id FROM t_meet_participants where t_meet_participants.select_participants=?2))  AS getPendingMeetingsCount,\n"
			+ " (SELECT COUNT(*) FROM t_message WHERE f_delete_flag = 'Y' AND f_message_status = 'UNSEEN' and f_user_project=?1 and f_message_receiver_persno=?2)   AS message_count_UNSEEN,\n"
			+ "(SELECT COUNT(*) FROM t_message WHERE f_delete_flag = 'Y' AND f_message_status = 'SEEN' and f_user_project=?1 and f_message_receiver_persno=?2)   AS message_count_SEEN,   \n"
			+ " (SELECT COUNT(*) FROM t_dak WHERE F_DAK_MARKED_TO_PERSNO=?2 and  f_dak_status='OPEN' and f_delete_flag='Y' and f_user_project=?1 )   AS dak_open_count    ,\n"
			+ "  (SELECT COUNT(*) FROM t_dak WHERE F_DAK_MARKED_TO_PERSNO=?2 and  f_dak_status='CLOSE' and f_delete_flag='Y' and f_user_project=?1 )   AS dak_closed_count    ,\n"
			+ "(SELECT COUNT(*) FROM t_notifications WHERE  F_NOTE_PROJECT=?1 and F_NOTE_ACK_STATUS='UNSEEN')AS Noti_Unseen_Count ,\n"
			+ "(SELECT COUNT(*) FROM t_notifications WHERE  F_NOTE_PROJECT=?1 and F_NOTE_ACK_STATUS='SEEN')   AS Noti_seen_Count ,\n"
			+ "(SELECT COUNT(*) FROM t_task WHERE  F_TASK_ASSIGNED_TO_PERSNO=?2  and f_user_project=?1 and F_TASK_STATUS not in ('100') and f_delete_flag='Y')   AS  task_pend_Count ,\n"
			+ "(SELECT COUNT(*) FROM t_task WHERE  F_TASK_ASSIGNED_TO_PERSNO=?2  and f_user_project=?1 and F_TASK_STATUS='100' and f_delete_flag='Y')   AS  task_completed_Count       \n"
			+ "FROM\n" + "    t_dak d,\n" + "    t_meeting m ,\n" + "    t_message a ,t_notifications n ,t_task t\n"
			+ "where\n" + "    m.f_part_name = a.f_part_name and\n" + "     a.f_part_name = n.f_note_part_name and \n"
			+ "     d.f_part_name = m.f_part_name and\n" + "     m.f_part_name=t.f_part_name", nativeQuery = true)
	public List<Object[]> AllDashBoardCounts(String ProjName,String UserPersno);

	@Query(value = " select  SUM(CASE WHEN u.f_active=0 AND u.f_delete_flag='Y' THEN 1 ELSE 0 END)AS InActiveUsers,\n"
			+ "     SUM(CASE WHEN u.f_active=1 AND u.f_delete_flag='Y' THEN 1 ELSE 0 END)AS activeUsers,\n"
			+ "     SUM(CASE WHEN u.f_active=0 AND u.f_delete_flag='N' THEN 1 ELSE 0 END)AS DeactivatedUsers,\n"
			+ "     SUM(CASE WHEN u.f_delete_flag IS null  THEN 1 ELSE 0 END)AS OtherUsers,\n"
			+ "     SUM(CASE WHEN u.f_delete_flag IN('Y','N') OR u.f_delete_flag IS null  THEN 1 ELSE 0 END)AS TotalUsers \n"
			+ "     , p.f_prj_name\n"
			+ "     from t_user u,t_user_projects up,t_projects p where up.user_f_user_name=u.f_user_name and  up.project_f_prj_id=p.f_prj_id   GROUP BY  p.f_prj_name", nativeQuery = true)
	public List<Object[]>countOfUsersPerProjects();

	@Query(value = "select p.f_prj_name AS ProjectName ,u.f_first_name AS FirstName,u.f_last_name AS LastName,u.f_user_name AS UserName,u.f_designation AS Designation from t_user u,t_user_projects up,t_projects p where  u.f_user_name=up.user_f_user_name and up.project_f_prj_id=p.f_prj_id and  u.f_active=1 AND u.f_delete_flag='Y'", nativeQuery = true)
	public List<Object[]> listOfUserLoggedIn();
	
    @Query(value=" select u.f_first_name || ' ' ||u.f_last_name ||'|'||u.f_persno AS UserName from t_user u,t_user_projects up,t_projects p where p.f_prj_name=?1 and u.f_user_name=up.user_f_user_name and up.project_f_prj_id=p.f_prj_id and  u.f_active=1 AND u.f_delete_flag='Y'",nativeQuery=true)
	public List<Map<String, Object>> listOfUsersPerProjects(String ProjName);
    
	@Query(value = "select to_char(f_timestamp,'Mon-YY') AS year_month,count(*) AS USER_COUNT from t_user GROUP BY TO_CHAR(F_TIMESTAMP,'Mon-YY') ORDER BY TO_DATE( TO_CHAR(F_TIMESTAMP,'Mon-YY'),'Mon-YYYY')", nativeQuery = true)
	public List<Object[]> countOfUsersPerMonth();

	@Query(value = "select  * from t_user where f_user_name=?1 and f_delete_flag='Y'", nativeQuery = true)
	public User getUserById(String userName);

	Optional<User> findByuserName(String userName);

	Optional<User> findByUserPassword(String userPassword);

	@Query(value = "select * from t_user where f_delete_flag='Y' order by f_timestamp desc", nativeQuery = true)
	public List<User> getbyYvalus(String sid);

	public Optional<User> findByuserNameOrPersno(String userName, String persno);

	List<User> findByActive(boolean active);

	Optional<User> findByUserName(String userName);

	// public User save(User userlist, String deleteuserip, String usernamedelete);
}
