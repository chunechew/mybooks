import axios from 'axios';
import React, {useRef} from 'react';
import {useSetClientState, useClientValue} from '../hooks/clientState';
import {QueryOptions} from 'react-query';

const Login = () => {
  useClientValue('username', '');
  useClientValue('password', '');
  useClientValue("loading", '');
  useClientValue("accessToken", "");
  useClientValue("refreshToken", "");

  const setUsernameClientState = useSetClientState('username');
  const setPasswordClientState = useSetClientState('password');
  const setAccessTokenState = useSetClientState('accessToken');
  const setRefreshTokenState = useSetClientState('refreshToken');

  const usernameInput = useRef(null);
  const passwordInput = useRef(null);
  const loadingText = useRef(null);

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
  
    console.log('Username', username);
    console.log('Password', password);
  
    const loadingState = "로딩 중";
    const successState = "로그인 성공";
    const failedState = "로그인 실패";
  
    changeText(loadingText.current, loadingState);

    await login(username, password, {})
      .then((response) => {
        changeText(loadingText.current, `${successState}: ${JSON.stringify(response)}`);
        setAccessTokenState(response.data.accessToken);
        setRefreshTokenState(response.data.refreshToken);
      })
      .catch((error) => {
        console.error(error);
        changeText(loadingText.current, `${failedState}: ${JSON.stringify(error)}`);
      });
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