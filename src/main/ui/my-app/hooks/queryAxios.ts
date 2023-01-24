import { useEffect, useState } from "react";
import { useQuery } from "react-query";
import apiClient from "../utils/http-common";

interface useLoginArgs {
    username: string,
    password: string,
};

export const useLogin = ({username, password}: useLoginArgs) => {
    const [result, setResult] = useState(null as any);

    const url = `/member/login`;

    const data = {
        username: username,
        password: password,
    };
    
    const {isLoading, refetch} =
        useQuery(
            "login",
            async () => {
                return await apiClient.post(url, data);
            },
            {
                enabled: false,
                retry: 1,
                onSuccess: (res) => {
                    const userInfo = res.data?.newTokens;
                    setResult(userInfo);
                },
                onError: (err) => {
                    console.error(err);
                    setResult({});
                },
            }
        );

    useEffect(() => {
        if (isLoading) setResult("searching...");
    }, [isLoading]);

    return [result, setResult];
}