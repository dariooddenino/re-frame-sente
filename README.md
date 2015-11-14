# re-frame-sente

A basic [re-frame](https://github.com/Day8/re-frame)
and [sente](https://github.com/ptaoussanis/sente) web app.

## Development Mode

### Run application:

#### Client
```
lein clean
lein figwheel dev
```
Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).

#### Server

```
lein run -m re-frame-sente.server
```

The port can be set as environment variable. The default is 3001.

## Production Build

```
lein clean
lein cljsbuild once min
```
