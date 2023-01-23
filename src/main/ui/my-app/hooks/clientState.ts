import { useQuery, useQueryClient } from 'react-query';

export const useSetClientState = (key: string) => {
  const queryClient = useQueryClient();
  return (state: any) => queryClient.setQueryData(key, state);
};

export const useClientValue = (key: string, initialData: any) =>
  useQuery(key, {
    initialData,
    staleTime: Infinity,
  }).data;