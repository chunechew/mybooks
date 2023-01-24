// Next.js API route support: https://nextjs.org/docs/api-routes/introduction
import type { NextApiRequest, NextApiResponse } from 'next'
import Cookies from "universal-cookie";
import formidable from 'formidable';

export const config = {
    api: {
      bodyParser: false
    }
}

export default async function handler(req: NextApiRequest, res: NextApiResponse<any>) {
  const parsedForm = await new Promise((resolve, reject) => {
    const form = formidable({ multiples: true });

    form.parse(req, (err: any, fields: any, files: any) => {
        if (err) reject({ err })
        resolve({ err, fields, files })
    }); 
  });

  const data = (parsedForm as any).fields.object;

  console.log(data);

  const cookies = new Cookies();

  cookies.set("userInfo", data, {
    maxAge: Number(((data.refreshTokenExpire - new Date().getTime()) / 1000).toFixed(0))
  });

  res.status(200).json({ response: 'OK' });
}
