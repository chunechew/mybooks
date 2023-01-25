import NextAuth from "next-auth";
import CredentialsProvider from "next-auth/providers/credentials";

export const authOptions = {
  providers: [
    CredentialsProvider({
        // The name to display on the sign in form (e.g. 'Sign in with...')
        name: 'Credentials',
        // The credentials is used to generate a suitable form on the sign in page.
        // You can specify whatever fields you are expecting to be submitted.
        // e.g. domain, username, password, 2FA token, etc.
        // You can pass any HTML attribute to the <input> tag through the object.
        credentials: {
          username: { label: "username", type: "text", placeholder: "jsmith" },
          password: { label: "password", type: "password" },
        },
        async authorize(credentials, req) {
          // You need to provide your own logic here that takes the credentials
          // submitted and returns either a object representing a user or value
          // that is false/null if the credentials are invalid.
          // e.g. return { id: 1, name: 'J Smith', email: 'jsmith@example.com' }
          // You can also use the `req` object to obtain additional parameters
          // (i.e., the request IP address)
          const body = JSON.stringify({
            username: credentials?.username || "",
            password: credentials?.password || "",
          });
          
          const res = await fetch(`${process.env.NEXT_PUBLIC_API_GATEWAY}/member/login`, {
            method: 'POST',
            body,
            headers: { "Content-Type": "application/json" }
          });

          const resJson: any = await res.json();

          const user = resJson.newTokens;
    
          // If no error and we have user data, return it
          if (res.ok && user) {
            return user;
          }
          // Return null if user data could not be retrieved
          return null;
        }
    }),
  ],
  secret: process.env.JWT_SECRET,
  pages: {
    signIn: '/login',
  },
  callbacks: {
    async jwt({ token, user, account }: any) {
      if (account && user) {
        return {
          ...token,
          ...user,
        }; // session({session, token}) 함수의 token 자리에 들어감
      }

      return token;
    },

    async session({ session, token }: any) {
      session.user = token;

      return session;
    },
  },
  theme: {
    colorScheme: 'light', // "auto" | "dark" | "light"
    brandColor: '#33FF5D', // Hex color code #33FF5D
    logo: '',// '/logo.png', // Absolute URL to image
  },
  // Enable debug messages in the console if you are having problems
  debug: process.env.NODE_ENV === 'development',
};

export default NextAuth(authOptions as any);