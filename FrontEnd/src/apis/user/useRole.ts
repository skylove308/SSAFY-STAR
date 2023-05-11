import { useQuery } from "react-query";
import { api } from "../api";
import { ROLE_URL } from "../../utils/urls";
import { useNavigate } from "react-router-dom";
import { isExpire } from "../error/isExpire";

const fetcher = () =>
  api
    .get(ROLE_URL, {
      headers: { Authorization: sessionStorage.getItem("accessToken") },
    })
    .then(({ data }) => data);

/**
 * 유저의 권한을 확인한다.
 * @returns
 */
const useUserRole = (setAdminCheck: (params: any) => void) => {
  const navigate = useNavigate();
  return useQuery("/userrole", () => fetcher(), {
    enabled: false,
    retry: 0,
    onSuccess: (data) => {
      if (data.value.includes("ROLE_ADMIN")) {
        setAdminCheck(true);
      } else {
        navigate("/");
      }
    },
    onError: (e: any) => {
      if (!isExpire(e.response.status)) {
        alert("잠시후에 시도해주세요");
      }
    },
  });
};

export default useUserRole;
