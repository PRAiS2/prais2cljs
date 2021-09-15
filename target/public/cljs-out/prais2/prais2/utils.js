// Compiled by ClojureScript 1.10.329 {:static-fns true, :optimize-constants true}
goog.provide('prais2.utils');
goog.require('cljs.core');
goog.require('cljs.core.constants');
goog.require('clojure.string');
goog.require('rum.core');
/**
 * value to pixel string
 */
prais2.utils.px = (function prais2$utils$px(value){
return [cljs.core.str.cljs$core$IFn$_invoke$arity$1(value),"px"].join('');
});
/**
 * value to percent string
 */
prais2.utils.pc = (function prais2$utils$pc(value){
if(cljs.core.truth_(value)){
return clojure.string.replace([cljs.core.str.cljs$core$IFn$_invoke$arity$1(value.toPrecision((3))),"%"].join(''),/[.]0?0/,"");
} else {
return null;
}
});
/**
 * tack !important on a string value
 */
prais2.utils.important = (function prais2$utils$important(str_val){
return [cljs.core.str.cljs$core$IFn$_invoke$arity$1(str_val)," !important"].join('');
});
/**
 * useful for mapping react keys to a content vector
 */
prais2.utils.key_with = (function prais2$utils$key_with(a,b){
return rum.core.with_key(b,a);
});
