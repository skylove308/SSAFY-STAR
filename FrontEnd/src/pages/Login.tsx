import BigButton from "../components/Button/BigButton";
import LinkButton from "../components/Button/LinkButton";
import MidButton from "../components/Button/MidButton";
import Input from "../components/Input/Input";
import EarthLayout from "../components/Layout/EarthLayout";

export default function Login() {
  return (
    <EarthLayout>
      <div className="flex flex-col justify-around h-full">
        <div>
          <span className="text-4xl block font-bold">Login</span>
          <span className="text-sm block">
            SsafyStar를 사용하기 위해 로그인 해 주세요
          </span>
        </div>
        <div className="mb-80">
          <Input id="email" type="input" label="이메일" />
          <Input id="password" type="password" label="비밀번호" />
          <div className="text-right">
            <LinkButton>비밀번호를 잊으셨나요?</LinkButton>
          </div>
        </div>

        <div className="flex flex-col gap-4">
          {/* oauth */}
          <div className="flex justify-center gap-16">
            <MidButton type="outline" value="구글 로그인" />
            <MidButton type="outline" value="kakao 로그인" />
          </div>
          <div className="flex justify-center">
            <BigButton value="로그인" />
          </div>
          <div className="text-right">
            <LinkButton>회원정보가 없으신가요? Register Herer</LinkButton>
          </div>
        </div>
      </div>
    </EarthLayout>
  );
}
