import { useQuery } from "react-query";
import { CARD_MYCARD_URL } from "../../utils/urls";
import { api } from "../api";
import { setCard } from "../../stores/card/cardsubmit";
import { useDispatch } from "react-redux";
import { useNavigate } from "react-router-dom";
const fetcher = () =>
  api
    .get(CARD_MYCARD_URL, {
      headers: { Authorization: sessionStorage.getItem("accessToken") },
    })
    .then(({ data }) => data);

const useMyCard = (setCompany:(params:string)=>void) => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  return useQuery("/mycardinfo", fetcher, {
    retry: 0,
    enabled: false,
    onSuccess: (data) => {
      setCompany(data.value.company);
      dispatch(setCard(data.value));
    },
    onError: (e: any) => {
      if (e.response.status === 403) {
        alert(e.response.data.message);
        navigate("/");
      } else {
        alert("잠시후 시도해 주세요.");
      }
    },
  });
};

export default useMyCard;
