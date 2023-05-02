import { useMutation } from "react-query";
import axios from "axios";
import { LOGIN_URL } from "../../utils/urls";
import { LoginType } from "../../types/LoginType";

const fetcher = (payload: LoginType) =>
  axios
    .post(LOGIN_URL, {
      accountId: payload.accountId,
      accountPwd: payload.accountPwd,
    })
    .then(({ data }) => data);

const useLogin = () => {
  return useMutation(fetcher, {
    onSuccess: (data) => {
      //토큰 저장
      sessionStorage.setItem("accessToken", data.value);
    },
    onError: () => {
      alert("아이디, 비밀번호를 확인해 주세요");
    },
  });
};

export default useLogin;
