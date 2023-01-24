import { useQuery, useQueryClient } from 'react-query';

export const useSetClientState = (key: string) => {
  const queryClient = useQueryClient();
  return (state: any) => {
    const newState = Object.assign(queryClient.getQueryData(key) as Object, state);
    return queryClient.setQueryData(key, newState);
  }
};

export const useClientValue = (key: string, initialData: any) => {
  const options = {
    initialData,
    staleTime: Infinity,
    keepPreviousData: true,
  };
  
  return useQuery(key, options).data;
};