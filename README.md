# Java-Happy-Eyeballs

## Descrição
Algoritmo repsonsável pela escolha da melhor rota entre os protocolos IPV6 e IPV4.  
Para maiores informações consulte a [RFC 6555](https://tools.ietf.org/html/rfc6555 "RFC 655").

## Como utilizar
Recupere o objeto único:  

`HappyEyeballs singleton = HappyEyeballs.getHappyEyeballsPadrao();`

Informe o nome do serviço e a porta de conecção:  

`singleton.obterIp("www.facasfjlerjwl.com.br", 80);`
