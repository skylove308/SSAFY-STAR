package com.ssafy.star.api.service;

import com.ssafy.star.common.auth.enumeration.GroupFlagEnum;
import com.ssafy.star.common.db.dto.request.CardRegistReqDto;
import com.ssafy.star.common.db.dto.request.CardUpdateReqDto;
import com.ssafy.star.common.db.dto.request.SearchConditionReqDto;
import com.ssafy.star.common.db.dto.response.CardDetailDto;
import com.ssafy.star.common.db.dto.response.ConstellationListDto;
import com.ssafy.star.common.db.dto.response.EdgeDto;
import com.ssafy.star.common.db.dto.response.GroupInfoDto;
import com.ssafy.star.common.db.entity.Card;
import com.ssafy.star.common.db.entity.Coordinate;
import com.ssafy.star.common.db.entity.Polygon;
import com.ssafy.star.common.db.entity.User;
import com.ssafy.star.common.db.repository.CardRepository;
import com.ssafy.star.common.db.repository.CompanyRepository;
import com.ssafy.star.common.db.repository.CoordinateRepository;
import com.ssafy.star.common.db.repository.PolygonRepository;
import com.ssafy.star.common.db.repository.UserRepository;
import com.ssafy.star.common.exception.CommonApiException;
import com.ssafy.star.common.provider.AuthProvider;
import com.ssafy.star.common.util.CallAPIUtil;
import com.ssafy.star.common.util.GeometryUtil;
import com.ssafy.star.common.util.constant.CommonErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Log4j2
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

	private final UserRepository userRepository;
	private final CompanyRepository companyRepository;
	private final CardRepository cardRepository;
	private final CoordinateRepository coordinateRepository;
	private final AuthProvider authProvider;
	private final PolygonRepository polygonRepository;
	// 반구의 반지름
	private final int RADIUS = 100;
	// Polygon Matrix의 행,열의 크기
	private final int SIZE = 91;
	private final int[] startYList = {30, 30, 30, 30, 40, 40, 40, 40, 50, 50, 50, 50, 60, 60, 60, 60};
	private final int[] startXList = {30, 40, 50, 60, 30, 40, 50, 60, 30, 40, 50, 60, 30, 40, 50, 60};
	private final int[] dy = {1, -1, 0, 0};
	private final int[] dx = {0, 0, 1, -1};
	private final Random random = new Random();

	@Override
	@Transactional
	public String updateBojTier() {
		long userId = authProvider.getUserIdFromPrincipal();

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CommonApiException(CommonErrorCode.USER_NOT_FOUND));

		Card card = Optional.ofNullable(user.getCard())
			.orElseThrow(() -> new CommonApiException(CommonErrorCode.NO_CARD_PROVIDED));

		String bojId = Optional.ofNullable(card.getBojId())
			.orElseThrow(() -> new CommonApiException(CommonErrorCode.NO_BOJ_ID_PROVIDED));

		String tier = CallAPIUtil.getUserTier(bojId);
		card.updateBojTier(tier);

		return tier;
	}

	@Override
	public String getBojTier(String bojId) {
		return CallAPIUtil.getUserTier(bojId);
	}

	@Override
	public ConstellationListDto getCardList(SearchConditionReqDto searchConditionReqDto) {
		List<Card> cardList = cardRepository.getAllCardListWithUser();
		// 바꿔야함
		// List<CardDetailDto> detailDtoList = setCoordinates(cardList, searchConditionReqDto.getStarCloudFlag());
		List<CardDetailDto> detailDtoList = setCoordinatesV1(cardList, "CAMPUS");

		List<EdgeDto> edgeDtoList = setEdges(detailDtoList);
		return new ConstellationListDto(detailDtoList, edgeDtoList);
	}

	@Override
	public ConstellationListDto getCardListV1(String searchColumn, String searchValue, String searchValue2,
		String searchValue3) {
		//이부분을 jpql써서 바꿔야할듯
		List<Card> cardList = new ArrayList<>();
		if (searchColumn != null) {
			if (searchColumn.equals("company") && searchValue != null) {
				cardList = cardRepository.getAllFilteredByCompany(searchValue);
			}
			if (searchColumn.equals("track") && searchValue != null) {
				cardList = cardRepository.getAllFilteredByTrack(searchValue);
			}
			if (searchColumn.equals("swTier") && searchValue != null) {
				cardList = cardRepository.getAllFilteredBySwTier(searchValue);
			}
			if (searchColumn.equals("major") && searchValue != null) {
				cardList = cardRepository.getAllFilteredByMajor(searchValue);
			}
			if (searchColumn.equals("bojTier") && searchValue != null) {
				cardList = cardRepository.getAllFilteredByBojTier(searchValue);
			}
			if (searchColumn.equals("generation") && searchValue != null) {
				cardList = cardRepository.getAllFilteredByGeneration(searchValue);
			}
			if (searchColumn.equals("campus") && searchValue != null) {
				String gen = searchValue;
				String cam = searchValue2;
				cardList = cardRepository.getAllFilteredByCampus(gen, cam);
			}
			if (searchColumn.equals("ban") && searchValue != null) {
				String gen = searchValue;
				String cam = searchValue2;
				String ban = searchValue3;
				cardList = cardRepository.getAllFilteredByBan(gen, cam, ban);
			}
			if (searchColumn.equals("")) {
				cardList = cardRepository.getAllCardListWithUser();
			}
		} else {
			cardList = cardRepository.getAllCardListWithUser();
		}
		List<CardDetailDto> detailDtoList = setCoordinatesV1(cardList, "CAMPUS");

		List<EdgeDto> edgeDtoList = setEdges(detailDtoList);
		return new ConstellationListDto(detailDtoList, edgeDtoList);
	}

	// 이게 찐 최종본이고, 모듈화 생략하고 여기에 일단 다 담을거임. ㅅㄱ
	@Override
	public ConstellationListDto getCardListV2(SearchConditionReqDto searchConditionReqDto) {

		List<Card> cardList = cardRepository.searchBySearchCondition(searchConditionReqDto);
		GroupFlagEnum groupFlag;
		try {
			groupFlag = GroupFlagEnum.valueOf(searchConditionReqDto.getGroupFlag().toUpperCase());
		} catch (Exception e) {
			throw new CommonApiException(CommonErrorCode.FAIL_TO_PARSE);
		}

		// 로그인한 유저의 아이디.
		long userId = -1L;
		try {
			userId = authProvider.getUserIdFromPrincipal();
		} catch (Exception e) {
		}

		// long userId = authProvider.getUserIdFromPrincipal();

		// 반구의 반지름
		List<Polygon> polygonList = polygonRepository.findAll();
		List<CardDetailDto> cardDetailDtoList = new ArrayList<>();

		// 1차원 polygionList를 2차원 polygonMatrix로 변경.
		Polygon[][] polygonMatrix = new Polygon[SIZE][SIZE];
		List<Integer> temp = new ArrayList<>();
		List<CardDetailDto> test2 = new ArrayList<>();
		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				if (polygonList.get(i * SIZE + j).getX() != null && polygonList.get(i * SIZE + j).getZ() >= 0.03) {
					polygonMatrix[i][j] = polygonList.get(i * SIZE + j);
					temp.add(i * SIZE + j);

					// test2.add(new CardDetailDto(cardList.get(0), polygonMatrix[i][j].getX()*RADIUS,
					// 	polygonMatrix[i][j].getZ()*RADIUS, polygonMatrix[i][j].getY()*RADIUS, false));
				}
			}
		}
		// if (true) {
		// 	return new ConstellationListDto(test2, null);
		// }

		Collections.shuffle(temp);
		Map<String, List<Card>> cardGroupMap = cardList.stream()
			.collect(Collectors.groupingBy(x -> x.getGroupFlag(groupFlag)));
		List<Integer> startPointIdx = new ArrayList<>();
		for (int i = 0; i < startYList.length; i++)
			startPointIdx.add(i);
		Collections.shuffle(startPointIdx);

		List<String> keyList = cardGroupMap.keySet().stream().collect(Collectors.toList());
		int groupSize = keyList.size();

		boolean[][] visited = new boolean[SIZE][SIZE];

		List<GroupInfoDto> groupInfoDtoList = new ArrayList<>();
		for (int i = 0; i < groupSize; i++) {
			String key = keyList.remove(0);
			int choice = startPointIdx.remove(0);
			int startY = startYList[choice];
			int startX = startXList[choice];
			int convPos = startY * SIZE + startX;
			Polygon polygon = polygonList.get(convPos);

			// x,y,z에 표시할 그룹 이름
			groupInfoDtoList.add(
				GroupInfoDto
					.builder()
					.groupName(key + groupFlag)
					.x(polygon.getX() * RADIUS)
					.y(polygon.getY() * RADIUS)
					.z(polygon.getZ() * RADIUS)
					.build());

			// x,y,z를 중심으로 그룹 내의 모든 카드를 배치

			List<Card> curGroupCardList = cardGroupMap.get(key);
			int willChooseCardCnt = curGroupCardList.size();
			Queue<int[]> queue = new ArrayDeque<>();
			List<int[]> choosePosList = new ArrayList<>();
			queue.add(new int[] {startY, startX});
			visited[startY][startX] = true;

			while (!queue.isEmpty() && choosePosList.size() < willChooseCardCnt) {
				int[] cur = queue.poll();
				choosePosList.add(cur);
				int cy = cur[0];
				int cx = cur[1];
				for (int k = 0; k < 4; k++) {
					int ny = cy + dy[k];
					int nx = cx + dx[k];
					if (0 <= ny && ny < SIZE && 0 <= nx && nx < SIZE && polygonMatrix[ny][nx] != null
						&& !visited[ny][nx]) {
						queue.add(new int[] {ny, nx});
						visited[ny][nx] = true;
					}
				}
			}

			// for (int[] a:choosePosList){
			// 	System.out.println(Arrays.toString(a));
			// }
			System.out.println(choosePosList.size());
			System.out.println(willChooseCardCnt);

			for (int j = 0; j < willChooseCardCnt; j++) {
				Card card = curGroupCardList.get(j);
				int[] choocePos = choosePosList.get(j);
				int idx = choocePos[0] * SIZE + choocePos[1];
				polygon = polygonList.get(idx);
				int y = idx / SIZE;
				int x = idx % SIZE;

				double myX = polygon.getX();
				double myY = polygon.getY();
				double myZ = polygon.getZ();
				double minX = polygon.getX();
				double minY = polygon.getY();
				double minZ = polygon.getZ();
				double maxX = polygon.getX();
				double maxY = polygon.getY();
				double maxZ = polygon.getZ();

				for (int k = 0; k < 4; k++) {
					int ny = y + dy[k];
					int nx = x + dx[k];
					if (0 <= ny && ny < SIZE && 0 <= nx && nx < SIZE) {
						Polygon tempPolygon = polygonList.get(ny * SIZE + nx);
						if (tempPolygon.getX() == null)
							continue;
						minX = Math.min(minX, (tempPolygon.getX() + myX * 3) / 4);
						minY = Math.min(minY, (tempPolygon.getY() + myY * 3) / 4);
						minZ = Math.min(minZ, (tempPolygon.getZ() + myZ * 3) / 4);
						maxX = Math.max(maxX, (tempPolygon.getX() + myX * 3) / 4);
						maxY = Math.max(maxY, (tempPolygon.getY() + myY * 3) / 4);
						maxZ = Math.max(maxZ, (tempPolygon.getZ() + myZ * 3) / 4);
					}
				}

				double finalX = random.nextDouble() * (maxX - minX) + minX;
				double finalY = random.nextDouble() * (maxY - minY) + minY;
				double finalZ = random.nextDouble() * (maxZ - minZ) + minZ;
				cardDetailDtoList.add(
					new CardDetailDto(card, finalX * RADIUS, finalZ * RADIUS, finalY * RADIUS,
						false));
			}

		}

		// List<EdgeDto> edgeDtoList = setEdges(detailDtoList);
		return new

			ConstellationListDto(cardDetailDtoList, null, groupInfoDtoList);

	}

	private List<EdgeDto> setEdges(List<CardDetailDto> detailDtoList) {
		List<EdgeDto> edgeList = new ArrayList<>();
		edgeList = GeometryUtil.getEdgeList(detailDtoList);
		return edgeList;
	}

	public List<CardDetailDto> setCoordinatesV2(List<Card> cardList, String groupFlag) {
		System.out.println(groupFlag);
		return null;
	}

	// starCloudFlag -> ENUM으로 바꿔야함.
	public List<CardDetailDto> setCoordinatesV1(List<Card> cardList, String starCloudFlag) {
		int cardCnt = cardList.size();
		//기본 천구
		int level;
		int r = 100;
		level = GeometryUtil.getLevelFromCardCnt(cardCnt);

		int vertices = GeometryUtil.getVerticesFromLevel(level);
		List<Integer> numbers = new ArrayList<>();
		for (int i = 0; i < vertices; i++) {
			numbers.add(i);
		}
		List<Integer> rs = new ArrayList<>();
		List<Integer> result = new ArrayList<>();
		Random random = new Random();
		for (int i = 0; i < cardCnt; i++) {
			int index = random.nextInt(numbers.size());
			result.add(numbers.remove(index));
			//			rs.add(random.nextInt(0));
			rs.add(0);
		}

		//level별 coordinate limit 걸기
		List<Coordinate> coordinateList = new ArrayList<>();
		if (level == 1) {
			coordinateList = coordinateRepository.findTop4ByOrderById();
		} else if (level == 2) {
			coordinateList = coordinateRepository.findTop17ByOrderById();
		} else if (level == 3) {
			coordinateList = coordinateRepository.findTop73ByOrderById();
		} else if (level == 4) {
			coordinateList = coordinateRepository.findTop305ByOrderById();
		} else if (level == 5) {
			coordinateList = coordinateRepository.findTop1249ByOrderById();
		} else {
			coordinateList = coordinateRepository.findAll();
		}

		List<CardDetailDto> detailDtoList = new ArrayList<>();
		if (starCloudFlag.equals("CGB")) {
			Map<String, List<Card>> temp = new HashMap<>();
			for (Card card : cardList) {
				String key = card.getCampus() + String.valueOf(card.getGeneration()) + String.valueOf(card.getBan());
				if (!temp.containsKey(key)) {
					temp.put(key, new ArrayList<>());
				}
				temp.get(key).add(card);
			}
		} else if (starCloudFlag.equals("CAMPUS")) {
			Map<String, List<Card>> temp = new HashMap<>();
			for (Card card : cardList) {
				String key = card.getCampus();
				if (!temp.containsKey(key)) {
					temp.put(key, new ArrayList<>());
				}
				temp.get(key).add(card);
			}
			for (String key : temp.keySet()) {
			}
		}

		long userId = -1L;
		try {
			userId = authProvider.getUserIdFromPrincipal();
		} catch (Exception e) {
		}

		for (int i = 0; i < cardCnt; i++) {
			int selected = result.get(i);
			int rr = rs.get(i);
			Card curCard = cardList.get(i);
			detailDtoList.add(new CardDetailDto(curCard, (r + rr) * coordinateList.get(selected).getX()
				, (r + rr) * coordinateList.get(selected).getY(), (r + rr) * coordinateList.get(selected).getZ(),
				curCard.getUser().getId() == userId
			));
		}
		return detailDtoList;
	}

	@Override
	public List<String> searchCompany(String query) {
		return companyRepository.searchCompanyList(query);
	}

	@Override
	@Transactional
	public void registCard(CardRegistReqDto cardRegistReqDto) {
		long userId = authProvider.getUserIdFromPrincipal();
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CommonApiException(CommonErrorCode.USER_NOT_FOUND));
		if (user.getCard() != null) {
			throw new CommonApiException(CommonErrorCode.ALEADY_EXIST_CARD);
		}
		user.setName(cardRegistReqDto.getName());
		Card card = cardRegistReqDto.of(user);
		cardRepository.save(card);
		user.setCard(card);
	}

	@Override
	@Transactional
	public void updateCard(CardUpdateReqDto cardUpdateReqDto) throws Exception {
		long userId = authProvider.getUserIdFromPrincipal();

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CommonApiException(CommonErrorCode.USER_NOT_FOUND));

		Card card = Optional.ofNullable(user.getCard())
			.orElseThrow(() -> new CommonApiException(CommonErrorCode.NO_CARD_PROVIDED));
		user.setName(cardUpdateReqDto.getName());
		card.of(cardUpdateReqDto);
	}


	@Override
	public CardDetailDto getMyCard() {
		long userId = authProvider.getUserIdFromPrincipal();

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CommonApiException(CommonErrorCode.USER_NOT_FOUND));

		Card card = Optional.ofNullable(user.getCard())
			.orElseThrow(() -> new CommonApiException(CommonErrorCode.NO_CARD_PROVIDED));
		CardDetailDto cardDetailDto = new CardDetailDto(card, 0, 0, 0, true);
		return cardDetailDto;
	}


}
