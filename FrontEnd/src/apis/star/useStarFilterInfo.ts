import { useMutation } from "react-query";
import axios from "axios";
import { STAR_FILTER_INFO_URL2 } from "../../utils/urls";
import { StarFilterType } from "../../types/StarFilterType";

//이미지 파일을 입력하기
const fetcher = (payload: StarFilterType) =>
  axios
    .post(STAR_FILTER_INFO_URL2, {
      ban: payload.ban,
      bojTier: payload.bojTier,
      campus: payload.campus,
      company: payload.company,
      generation: payload.generation,
      major: payload.major,
      role: payload.role,
      swTier: payload.swTier,
      track: payload.track,
      groupFlag: payload.groupFlag,
    })
    .then(({ data }) => data.value);

const useStarFilterInfo = () => {
  return useMutation(fetcher, {
    onSuccess: (data) => {
      console.log("별자리 필터 검색 불러오기 성공", data);
    },
    onError: (e) => {
      alert("표시되는 별의 수가 너무 많아 그룹화가 어렵습니다!");
      console.log("별자리 필터 검색 불러오기 실패", e);
    },
  });
};

export default useStarFilterInfo;
