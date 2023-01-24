import { useQuery, useQueryClient } from 'react-query';

export const useSetClientState = (key: string) => {
  const queryClient = useQueryClient();
  return (state: any) => {
    let newState;
    let oldState = queryClient.getQueryData(key);
    if(state instanceof Object && oldState instanceof Object) {
      newState = Object.assign(oldState, state);
    } else {
      newState = state;
    }
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