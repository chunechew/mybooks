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

  const data = (parsedForm as any).fields.key;

  console.log("data: ", data);

  const cookies = new Cookies();

  cookies.remove(data);

  res.status(200).json({ response: 'OK' });
}
