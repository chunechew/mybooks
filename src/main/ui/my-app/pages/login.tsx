import axios from 'axios';
import React, { useEffect, useRef } from 'react';
import { QueryOptions, useQueryClient } from 'react-query';
import { useClientValue, useSetClientState } from '../hooks/clientState';
import Cookies from 'universal-cookie';

const Login = () => {
  const queryClient = useQueryClient();
  let isLoggedIn = queryClient.getQueryData("loggedIn") as boolean || false;

  useClientValue('username', '');
  useClientValue('password', '');
  useClientValue('loading', '');
  useClientValue("userInfo", {
    accessToken: "",
    accessTokenExpire: "",
    refreshToken: "",
    refreshTokenExpire: "",
    username: "",
    email: "",
    role: "",
  });
  useClientValue("loggedIn", false);

  const setUsernameClientState = useSetClientState('username');
  const setPasswordClientState = useSetClientState('password');
  const setUserInfoState = useSetClientState('userInfo');
  const setLoggedInState = useSetClientState('loggedIn');

  const usernameInput = useRef(null);
  const passwordInput = useRef(null);
  const loadingText = useRef(null);

  // React hook의 사용 위치 제한 우회용 코드
  const useUpdate = (isLoggedIn: boolean) => {
    useEffect(() => {
      setLoggedInState(isLoggedIn);
    });

    return { setLoggedIn: setLoggedInState };
  }

  const {setLoggedIn} = useUpdate(isLoggedIn);

  const onUsernameHandler = (e: React.ChangeEvent<HTMLInputElement>) => {
    setUsernameClientState(e.currentTarget.value);
  };
  
  const onPasswordHandler = (e: React.ChangeEvent<HTMLInputElement>) => {
    setPasswordClientState(e.currentTarget.value);
  };
  
  const onSubmitHandler = async (e: React.SyntheticEvent, currUsername: any, currPassword: any) => {
    e.preventDefault();

    const username = currUsername?.value || "";
    const password = currPassword?.value || "";
  
    const loadingState = "로딩 중";
    const successState = "로그인 성공";
    const failedState = "로그인 실패";
  
    changeText(loadingText.current, loadingState);

    await login(username, password, {})
      .then(/*async*/ (response) => {
        changeText(loadingText.current, `${successState}: ${JSON.stringify(response)}`);
        setUserInfoState(response.newTokens);
        
        const cookies = new Cookies();

        cookies.set("userInfo", response.newTokens, {
          maxAge: Number(((response.newTokens.refreshTokenExpire - new Date().getTime()) / 1000).toFixed(0))
        });

        console.log(cookies.getAll());

        isLoggedIn = true;

        // await sendCookie(response.newTokens);
      })
      .catch((error) => {
        console.error(error);
        changeText(loadingText.current, `${failedState}: ${JSON.stringify(error)}`);
      });

      setLoggedIn(isLoggedIn);
  };

  const changeText = (curr: any, stateText: string) => {
    if(curr) {
      curr.innerText = stateText;
    }
  };
  
  const login = async (username: string, password: string, options: QueryOptions) => {
    const url = `${process.env.NEXT_PUBLIC_API_GATEWAY}/member/login`;

    console.log(process.env, process.env.NEXT_PUBLIC_API_GATEWAY, process.env.NODE_ENV);
    
    const data = {
      username: username,
      password: password
    };
  
    const response = await axios({
      method: 'post',
      url: url,
      data: data,
    }).then((res) => res.data);

    return response;
  };

  // 서버 사이드에도 쿠키 저장 요청
  /*const sendCookie = async(object: object) => {
    const url = `${process.env.NEXT_PUBLIC_NEXT_API_GATEWAY}/setCookieOnServer`;

    console.log(process.env, process.env.NEXT_PUBLIC_NEXT_API_GATEWAY, process.env.NODE_ENV);
    
    const data = {
      object 
    };
  
    const response = await axios({
      method: 'post',
      url: url,
      data: data,
    }).then((res) => res.data);

    return response;
  }*/

  return (
    <>
      <div style={{ 
        display: 'flex', justifyContent: 'center', alignItems: 'center', 
        width: '100%', height: 'calc(100vh - 120px)'
      }}>
        <form style={{ display: 'flex', flexDirection: 'column'}}
            onSubmit={async (e) => await onSubmitHandler(e, usernameInput.current as any, passwordInput.current as any)}
        >
            <label>아이디</label>
            <input type='text' ref={usernameInput} onChange={onUsernameHandler}/>
            <label>패스워드</label>
            <input type='password' ref={passwordInput} onChange={onPasswordHandler}/>
            <br />
            <button formAction=''>
                로그인
            </button>
        </form>
      </div>
      <div style={{
        textAlign: 'center',
      }} ref={loadingText}></div>
    </>
  );
}

export default Login;