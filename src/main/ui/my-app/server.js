const express = require('express');
const next = require('next');
const url = require('url');
const lruCache = require('lru-cache');

const ssrCache = new lruCache({
    max: 100,
    options: {
        ttl: 1000 * 60,
    },
});

const port = 3000;
const dev = process.env.NODE_ENV !== 'production';
const app = next({dev});
const handle = app.getRequestHandler();

app.prepare().then(() => {
    const server = express();

    /*server.get('/page/:id', (req, res) => {
        res.redirect(`page${req.params.id}`);
    });

    server.get(/^\/page[1-9]/, (req, res) => {
        return renderAndCache(req, res);
    });*/

    server.get('*', (req, res) => {
        return handle(req, res);
    });

    server.post('*', (req, res) => {
        return handle(req, res);
    });


    server.listen(port, (err) => {
        if(err) throw err;
        console.log('> Ready.');
    })
});

// 동적 페이지와 정적 페이지를 동시에 처리
const fs = require('fs');

const pretenderList = [
    {name: 'page1', path: '/page1'},
    {name: 'page2-hello', path: '/page2?text=hello'},
    {name: 'page2-world', path: '/page2?text=world'},
];

const pretenderCache = {};

if(!dev) {
    for(const info of pretenderList) {
        const {name, path} = info;
        const html = fs.readFileSync(`./out/${name}.html`, 'utf8');
        pretenderCache[path] = html;
    }
}

async function renderAndCache(req, res) {
    const parsedUrl = url.parse(req.url, true);
    const cacheKey = parsedUrl.path;

    if(ssrCache.has(cacheKey)) {
        console.log('캐시 사용');
        res.send(ssrCache.get(cacheKey));
        return;
    }

    const {query, pathname} = parsedUrl;

    try {
        const html = await app.renderToHTML(req, res, pathname, query);

        if(res.statusCode === 200) {
            ssrCache.set(cacheKey, html);
        }

        res.send(html);
    } catch(err) {
        app.renderError(err, req, res, pathname, query);
    }
}