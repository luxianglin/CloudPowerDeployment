%画图 自定义函数drawplot 参数 x y 
function drawplot(x, y1,t1,y2,t2,y3,t3,y4,t4,xtext,ytext,titletext)
plot(x, y1,'-g',x,y2,':r',x,y3,'--b',x,y4,'-.k'); %%使用matlab函数plot()作图
legend(t1,t2,t3,t4)
grid on %网格显示
xlabel(xtext)
ylabel(ytext)
title(titletext)