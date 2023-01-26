import { useState } from 'react';
import { getCsrfToken, signIn } from 'next-auth/react';
import { Formik, Field, ErrorMessage } from 'formik';
import * as Yup from 'yup';
import { useRouter } from 'next/router';

interface SignInArgs {
  csrfToken: string,
}

export default function SignIn({ csrfToken } : SignInArgs) {
  const router = useRouter();
  const [error, setError] = useState(null);

  return (
    <>
      <Formik
        initialValues={{ username: '', password: '', tenantKey: '' }}
        validationSchema={Yup.object({
          username: Yup.string()
            .max(30, '아이디는 30자 이내로 입력해 주세요.')
            // .email('Invalid email address')
            .required('아이디를 입력해 주세요.'),
          password: Yup.string().required('비밀번호를 입력해 주세요.'),
         })}
        onSubmit={async (values: any, { setSubmitting }) => {
          const res: any = await signIn('credentials', {
            redirect: false,
            username: values.username,
            password: values.password,
            callbackUrl: `${window.location.origin}`,
          });
          if (res?.error) {
            setError(res.error);
          } else {
            setError(null);
          }
          if (res.url) router.push(res.url);
          setSubmitting(false);
        }}
      >
        {(formik) => (
          <form onSubmit={formik.handleSubmit}>
            <div 
            className="flex flex-col items-center 
            justify-center min-h-screen py-2 shadow-lg">
              <div className="bg-gray-200 shadow-md rounded px-8 pt-6 pb-8 mb-4">
                <input
                  name="csrfToken"
                  type="hidden"
                  defaultValue={csrfToken}
                />

                <div className="text-red-400 text-md text-center rounded p-2">
                  {error}
                </div>
                <div className="mb-4">
                  <label
                    htmlFor="username"
                    className="uppercase text-sm text-gray-600 font-bold"
                  >
                    아이디
                    <Field
                      name="username"
                      aria-label="아이디를 입력해 주세요."
                      aria-required="true"
                      type="text"
                      className="w-full text-gray-900 mt-2 p-3"
                    />
                  </label>

                  <div className="text-red-600 text-sm">
                    <ErrorMessage name="username" />
                  </div>
                </div>
                <div className="mb-6">
                  <label
                    htmlFor="password"
                    className="uppercase text-sm text-gray-600 font-bold"
                  >
                    비밀번호
                    <Field
                      name="password"
                      aria-label="enter your password"
                      aria-required="true"
                      type="password"
                      className="w-full text-gray-900 mt-2 p-3"
                    />
                  </label>

                  <div className="text-red-600 text-sm">
                    <ErrorMessage name="password" />
                  </div>
                </div>
                <div className="flex items-center justify-center">
                  <button
                    type="submit"
                    className="bg-green-400 text-gray-100 p-3 rounded-lg w-full"
                  >
                    {formik.isSubmitting ? '잠시만 기다려 주세요...' : '로그인'}
                  </button>
                </div>
              </div>
            </div>
          </form>
        )}
      </Formik>
    </>
  );
}

// This is the recommended way for Next.js 9.3 or newer
export async function getServerSideProps(context: any) {
  return {
    props: {
      csrfToken: await getCsrfToken(context),
    },
  };
}