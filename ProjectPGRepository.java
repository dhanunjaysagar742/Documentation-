package com.drdl.plm.Repository;



import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.drdl.plm.entity.Programme;
import com.drdl.plm.entity.Project;
import com.drdl.plm.entity.ProjectPG;

@Repository
public interface ProjectPGRepository extends JpaRepository<ProjectPG ,String>{

	 @Query( value="select * from t_projects ", nativeQuery=true) 
	 public Set<Project> getUsersWithProjects(String userName);
	 
	 
	 
		@Query(value="select * from t_projects where user_name=?1",nativeQuery = true)
		 public Set<Project> allProjects(@Param("userName")String user_name);
		
		@Query(value="select f_prj_name from t_projects ",nativeQuery=true)
		public List<String> getProjectNames();
		



		@Query(value="select * from t_projects where f_delete_flag='Y' order by f_prj_id desc",nativeQuery=true)
		List<Project> getbyYvalus(String sid);



		public List<Project> findByFprojectname(String fprojectname);
		@Query(value="select F_PRJ_ID from t_projects where F_PRJ_NAME=?1",nativeQuery=true)
		String getProjectIdByProjectName(String projectName);
	
	 
}
