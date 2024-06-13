package com.ex.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.h2.mvstore.Page.PageReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.ex.data.QuestionDTO;
import com.ex.entity.QuestionEntity;
import com.ex.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

// Controller -- Service -- Repository -- Entity (DB-Table)
public class QuestionService {
	private final QuestionRepository questionRepository;	// repository 를 쓰겠다고 선언

	// 페이징 처리 - 페이지별로 나눠서 가져오는 역할 (한 번에 다 가져오는 것이 아니라, 필요한 부분만 나눠서 보여줌)
	public Page<QuestionEntity> questionAll(int page){	
		List<Sort.Order> sorts = new ArrayList<>();	
		// sorts 라는 리스트를 만들어서 나중에 질문들을 어떤 기준으로 정렬할지 정함.
		sorts.add(Sort.Order.desc("createDate"));	
		// 정렬을 날짜(createDate) 기준으로 내림차순함. (글 번호는 자동 증가되고 있는 중)
		// 가장 최근에 쓴 글부터 적용한다.
		Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));	
		// 페이지 설정하는 것 - page: 어떤 페이지를 볼지 결정하는 번호
		// 10 - 몇개의 질문을 보여줄지 - 한 페이지 당 열개씩. 페이지넘 개념. 페이지 번호를 누르면 넘어가야 한다
		// 페이지 번호를 매개변수로 받고 1페이지~10페이지 알아서 잘라줌
		return questionRepository.findAll(pageable);	
		// 파인드 올을 리턴에 바로 씀 - findAll 메서드에게 pageable 을 보내면 됨 - list 가 아니라 페이지로 받음
		// 설정한 페이지와 정렬 기준에 따라 데이터베이스에서 질문들을 가져와 페이지 형식으로 반환
		// 요약하자면, 이 함수는 데이터베이스에서 모든 질문들을 가져오는데, 한 번에 10개씩 페이지별로 나눠서 가져옵니다. 
		//가장 최근에 작성된 질문이 먼저 나오도록 정렬합니다. 페이지 번호를 입력하면 해당 페이지에 있는 질문들을 반환합니다.
	}
	
	// 엔티티에서 아이디를 가져오는건가? 그런 메서드? 옵셔널 - 널값을 넘기지 않기 위해서 > op 객체가 비어 있으면 트루. 비어있지 않을 때 객체를 넣어줘야함
	//엔티티만 리턴하는 것을 하나 더 만들어주는 것임 - 내가 찾으려하는 글 번호가 있는지 . 가장 최근 작업이 무엇인지 알고 싶으면 else 문이 필요할듯
	public QuestionEntity questionEntityId(Integer id) {	
		// id 라는 숫자를 받아서 그에 해당하는 질문을 찾아줌. 퀘스쳔 엔티티 객체 반환
		Optional<QuestionEntity> op = questionRepository.findById(id);
		// 데이터베이스에서 주어진 id 에 해당하는 질문을 찾고 결과를 op 라는 이름의 변수에 담음. Optional 은 값이 있을 수도 있고 없을 수도 있는 것을 나타냄
		QuestionEntity qe = null;
		// qe 변수에 나중에 찾은 질문을 담음
		if(!op.isEmpty()) {qe=op.get();}
		// 질문이 실제로 데이터베이스에서 발견되었는지 확인 - 질문이 존재하면 true 
		//  만약 질문이 존재하면 op.get()을 사용해서 질문을 가져오고 qe 변수에 담음
		return qe;
	}
	
	// for 문이 필요 없음 파인드바이아이디로 하나만 꺼내면 되기 때문에 - 이건 엔티티랑 디티오 같이 리턴하는 것
	// 데이터베이스에서 id 에 해당하는 질문을 찾아서 ,그 질문을 QuestionDTO 로 변환해서 반환
	public QuestionDTO questionId(Integer id) {
		QuestionEntity qe = questionRepository.findById(id).get();
		// 데이터베이스에 주어진 id 에 해당하는 질문을 찾는 것. qe - 변수 QuestionEntity 라는 질문 객체
		QuestionDTO questionDTO = QuestionDTO.builder()
												.id(qe.getId()).subject(qe.getSubject())
												.content(qe.getContent()).createDate(qe.getCreateDate())
												.answerList(qe.getAnswerList())
												.build();
		// qe 객체에서 id 를 가져와서 DTO 에 넣는 것 --> 이 작업 후 build 를 호출해서 모든 정보를 담은 QuestionDTO 객체를 만듦
		return questionDTO;
		// 이 DTO 안에는 우리가 찾은 질문의 정보가 담겨 있음.
	}
	// 새로운 질문을 등록하는 메서드 - QuestionDTO 라는 질문 정보를 받아서 새로운 질문을 데이터베이스에 저장함
	public void create(QuestionDTO questionDTO) {
		QuestionEntity qe = QuestionEntity.builder()
									.subject(questionDTO.getSubject())
									.content(questionDTO.getContent())
									.createDate(LocalDateTime.now())
									.build();
		// 등록하려는 질문의 제목을 가져와서 QuestionEntity 의 제목에 넣음. 
		// 그리고 마지막으로 build 를 호출해서 모든 정보를 담은 QuestionEntity 객체를 만들고 qe 라는 변수에 담음
		questionRepository.save(qe);
		// 만든 객체를 데이터베이스에 저장
	}	
}			

