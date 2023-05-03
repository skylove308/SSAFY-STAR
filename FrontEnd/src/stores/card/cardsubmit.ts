import { createSlice } from "@reduxjs/toolkit";

interface cardState {
  card: {
    ban: string; //1학기 기준 반
    blogAddr: string; //블로그
    bojid: string; //백준아이디
    bojTier: string; //백준티어
    campus: string; //캠퍼스 지역
    company: string; //회사
    content: string; //한마디
    etc: string; //더할말
    generation: string; //기수
    githubId: string; //깃헙주소
    major: string; //전공
    role: string; //FE,BE
    swTier: string; //소프트티어
    track: string; //트랙
  };
}

const initialState: cardState = {
  card: {
    ban: "", //1학기 기준 반
    blogAddr: "", //블로그
    bojid: "", //백준아이디
    bojTier: "", //백준티어
    campus: "", //캠퍼스 지역
    company: "", //회사
    content: "", //한마디
    etc: "", //더할말
    generation: "", //기수
    githubId: "", //깃헙주소
    major: "", //전공
    role: "", //FE,BE
    swTier: "", //소프트티어
    track: "", //트랙
  },
};

const cardSlice = createSlice({
  name: "card",
  initialState,
  reducers: {
    setCard(state, action) {
      state.card = action.payload;
    },
    resetCard(state) {
      state.card = {
        ban: "", //1학기 기준 반
        blogAddr: "", //블로그
        bojid: "", //백준아이디
        bojTier: "", //백준티어
        campus: "", //캠퍼스 지역
        company: "", //회사
        content: "", //한마디
        etc: "", //더할말
        generation: "", //기수
        githubId: "", //깃헙주소
        major: "", //전공
        role: "", //FE,BE
        swTier: "", //소프트티어
        track: "", //트랙
      };
    },
  },
});

export const { setCard, resetCard } = cardSlice.actions;

export default cardSlice.reducer;