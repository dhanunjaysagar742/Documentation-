package com.drdl.plm.controller;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.drdl.plm.Repository.ProjectPGRepository;
import com.drdl.plm.Repository.ProjectRepository;
import com.drdl.plm.entity.Project;
import com.drdl.plm.entity.ProjectPG;
import com.drdl.plm.service.globalfilepathServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/foldersize")
public class FolderSizeController {

	@Value("${file.storage.path}")
	private String UPLOADED_FOLDER;

	@Autowired
	private ProjectPGRepository projRepository;

	@Autowired
	private RestTemplate restTemplate;

	@GetMapping("/graph")
	public String graphChart() throws JsonProcessingException {

		String jsonObject = null;
		String folderPath = UPLOADED_FOLDER + "heirarchy.json";

		System.out.println("folderPath------" + folderPath);

		Path startPath = Paths.get(folderPath);
		String url = "http://10.66.40.212/" + startPath;
		System.err.println(url);
		String json = restTemplate.getForObject(url, String.class);
		ObjectMapper objectMapper = new ObjectMapper();
		jsonObject = objectMapper.writeValueAsString(json);
		return jsonObject;

	}

	private final ObjectMapper objectMapper = new ObjectMapper();

	@GetMapping("/{projectName}") // API= fetching folder size respect to Project Name
	public double assignpath(@PathVariable String projectName) throws IOException {
		List<Project> objproject = projRepository.findByFprojectname(projectName);
		String globalfilepath = objproject.get(0).getFolderpath();
		System.out.println("globalfilepath----" + globalfilepath);

		String folderPath = UPLOADED_FOLDER + globalfilepath;

		System.out.println("folderPath------" + folderPath);

		Path startPath = Paths.get(folderPath);

		long sizeInBytes = calculateSize(startPath);
		System.out.println("sizeInBytes------" + sizeInBytes);
		return sizeInBytes / 1024.0;

	}

	@GetMapping("/") // API= fetching folder size(kg,mb,gb) respect to all Projects
	public String assignpath() throws IOException {

		long totalSize = 0;
		List<ProjectPG> objproject = projRepository.findAll();

		List<ProjectPG> collect = objproject.stream()
				.filter(p -> p != null && p.getFprojectname() != null && p.getFolderpath() != null)
				.collect(Collectors.toMap(ProjectPG::getFprojectname, p -> p, (existing, replacement) -> existing))
				.values().stream().collect(Collectors.toList());

		System.err.println("collect-----------------" + collect.toString());

		List<Map<String, Object>> result = new ArrayList<>();
		String json = null;
		for (ProjectPG pr : collect) {

			System.err.println(pr.getFolderpath());
			String globalfilepath = pr.getFolderpath();
			System.out.println("globalfilepath----" + globalfilepath);

			String folderPath = UPLOADED_FOLDER + globalfilepath;

			System.out.println("folderPath------" + folderPath);

			Path startPath = Paths.get(folderPath);

			if (!Files.exists(startPath) || !Files.isDirectory(startPath)) {
				throw new IllegalArgumentException("The Specified path is not a valid directory:" + startPath);
			}

			totalSize = calculateSize(startPath);
			Map<String, Object> sizeMap = new HashMap<>();
			sizeMap.put("name", pr.getFprojectname());
			sizeMap.put("KB", totalSize / 1024);
			sizeMap.put("MB", totalSize / (1024 * 1024));
			sizeMap.put("GB", totalSize / (1024 * 1024 * 1024));
			result.add(sizeMap);
			System.err.println(sizeMap.toString());
		}
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			json = objectMapper.writeValueAsString(result);
			System.err.println("jsonnnnnn----" + json);
		} catch (Exception e) {
			// TODO: handle exception
			e.getMessage();
		}

		return json;

	}

	@GetMapping("/total") // API= fetching folder size(in GB) respect to all Projects
	public String totalSizePerProject() throws IOException {

		double totalSize = 0;
		List<ProjectPG> objproject = projRepository.findAll();

		List<ProjectPG> collect = objproject.stream()
				.filter(p -> p != null && p.getFprojectname() != null && p.getFolderpath() != null)
				.collect(Collectors.toMap(ProjectPG::getFprojectname, p -> p, (existing, replacement) -> existing))
				.values().stream().collect(Collectors.toList());

		// System.err.println("collect-----------------" + collect.toString());

		List<Map<String, Object>> result = new ArrayList<>();
		String json = null;
		for (ProjectPG pr : collect) {

			System.err.println(pr.getFolderpath());
			String globalfilepath = pr.getFolderpath();
			System.out.println("globalfilepath----" + globalfilepath);

			String folderPath = UPLOADED_FOLDER + globalfilepath;

			System.out.println("folderPath------" + folderPath);

			Path startPath = Paths.get(folderPath);

			if (!Files.exists(startPath) || !Files.isDirectory(startPath)) {
				throw new IllegalArgumentException("The Specified path is not a valid directory:" + startPath);
			}

			totalSize = calculateSize(startPath);
			double sizeInKB = totalSize / (1024.0 * 1024.0 * 1024.0);
			// System.err.println("totalSize from API-----"+totalSize);
			Map<String, Object> sizeMap = new HashMap<>();
			sizeMap.put("name", pr.getFprojectname());
			sizeMap.put("GB", Math.round(sizeInKB * 100.0) / 100.0);
			result.add(sizeMap);
			// System.err.println(sizeMap.toString());
		}
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			json = objectMapper.writeValueAsString(result);
			System.err.println("jsonnnnnn----" + json);
		} catch (Exception e) {
			// TODO: handle exception
			e.getMessage();
		}

		return json;

	}

	private long calculateSize(Path path) throws IOException {
		long size = 0;
		if (Files.isDirectory(path)) {
			DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path);
			for (Path subPath : directoryStream) {
				size += calculateSize(subPath);
				System.err.println("from calculateSzie method()----------" + size + "-------------path---------------"
						+ path.getFileName());
			}

		} else {
			size += Files.size(path);
			System.err.println("from calculateSzie method()----------" + size + "-------------path---------------"
					+ path.getFileName());

		}
		System.err.println("from calculateSize()--total size----" + size);
		return size;
	}

	@GetMapping("/totalSizePerDateWise") // API==total size in GB based on project and today's date
	public String totalSizePerDateWise() throws IOException, ParseException {

		String json = null;

		int i = 0;
		long size = 0;
		double sizeInKB = 0;
		LocalDate today = null;

		List<Map<String, Object>> totalProjectData = new ArrayList<>();
		List<ProjectPG> objproject = projRepository.findAll();

		List<ProjectPG> collect = objproject.stream()
				.filter(p -> p != null && p.getFprojectname() != null && p.getFolderpath() != null)
				.collect(Collectors.toMap(ProjectPG::getFprojectname, p -> p, (existing, replacement) -> existing))
				.values().stream().collect(Collectors.toList());

//		System.err.println("collect-----------------" + collect.toString());

		for (ProjectPG pr : collect) {
			sizeInKB = 0;
			size = 0;

			BigDecimal bde = new BigDecimal(0);
			Map<String, Object> projectData = new HashMap<>();

			System.err
					.println(pr.getFolderpath() + "============pr.getFprojectname()===========" + pr.getFprojectname());
			String globalfilepath = pr.getFolderpath();
			System.out.println("globalfilepath----" + globalfilepath);

			String folderPath = UPLOADED_FOLDER + globalfilepath;

			// System.out.println("folderPath------" + folderPath);

			Path startPath = Paths.get(folderPath);

			if (!Files.exists(startPath) || !Files.isDirectory(startPath)) {
				throw new IllegalArgumentException("The Specified path is not a valid directory:" + startPath);
			}

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
			if (Files.isDirectory(startPath)) {
				DirectoryStream<Path> directoryStream = Files.newDirectoryStream(startPath);
				for (Path subPath : directoryStream) {
					BasicFileAttributes attr = Files.readAttributes(subPath, BasicFileAttributes.class);
					Instant instant = attr.creationTime().toInstant();
					LocalDate fileCreationTime = LocalDate.ofInstant(instant, ZoneId.systemDefault());
					String creationformat = fileCreationTime.format(formatter);
					today = LocalDate.now();
					String todayFormat = today.format(formatter);
					SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MMM-yyyy");
					Date parse = sdf1.parse(todayFormat);
					Date parse1 = sdf1.parse(creationformat);
					int compareTo = parse.compareTo(parse1);
					System.err.println("parse1==============" + parse1 + "parse====" + parse
							+ "======================compareTo========================" + compareTo + "project name---"
							+ pr.getFprojectname() + "project path----");
					Map<LocalDate, Long> dailySizes = new HashMap<>();

					if (compareTo == 0) {
						System.err.println("compareTo---" + compareTo + "projectname" + pr.getFprojectname());
						size += calculateSizeperDate(subPath);
						Long merge = dailySizes.merge(today, size, Long::sum);
						// System.out.println("projectname----"+pr.getFprojectname());
						// System.err.println("dailySizes----"+dailySizes.toString());
						// System.err.println("merge------------"+merge);
						double totalSize = (double) merge;
						System.err.println("totalSize----" + totalSize);
						// sizeInKB=totalSize/(1024*1024*1024);
						sizeInKB = totalSize / (1024.0 * 1024.0 * 1024.0);

						// bde=new BigDecimal(sizeInKB);
						// bd=bde.setScale(2,RoundingMode.HALF_UP);
						System.err.println("sizeInKB----" + sizeInKB);

					}

				}

				// float f=(float)Math.round(sizeInKB*100.0)/100.0f;

				projectData.put("name", pr.getFprojectname());
				projectData.put("Date", today.format(formatter));
				projectData.put("Total Size", Math.round(sizeInKB * 100.0) / 100.0);
				System.err.println("projectData----------------" + projectData);
				totalProjectData.add(projectData);
				System.err.println("totalProjectData----------------" + totalProjectData);
			}
			size = (long) 0.0;
		}
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			json = objectMapper.writeValueAsString(totalProjectData);
			// System.err.println("jsonnnnnn----" + json);
		} catch (Exception e) {
			// TODO: handle exception
			e.getMessage();
		}

		return json;

	}

	private long calculateSizeperDate(Path path) throws IOException {
		long size44 = 0;

		if (Files.isDirectory(path)) {
			DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path);
			for (Path subPath : directoryStream) {
				size44 += calculateSizeperDate(subPath);
				System.out.println("from calculateSize method()-" + size44 + "-path--" + path.getFileName()
						+ "pathname---" + path.toAbsolutePath());
			}

		} else {
			size44 += Files.size(path);
			System.out.println("from calculateSize method()----------" + size44 + "-path--" + path.getFileName()
					+ "pathname---" + path.toAbsolutePath());

		}
		System.err.println("from calculateSize()--total size----" + size44);
		return size44;
	}
	
	@GetMapping("/projectname")
	public String getProjectNames(){
		 List<String> projectNames = projRepository.getProjectNames();
		 String json=null;
		 try {
				ObjectMapper objectMapper = new ObjectMapper();
				json = objectMapper.writeValueAsString(projectNames);
				System.err.println("jsonnnnnn----" + json);
			} catch (Exception e) {
				// TODO: handle exception
				e.getMessage();
			}

			return json;
	}

}
