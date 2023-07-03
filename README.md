# My Day Planner

## Obsah repositáře
* zdrojové kódy aplikace v Javě a soubor maven
* uživatelská dokumentace
* soubory php, které jsou nahrané i na serveru, 
přes který se aplikace připojuje k databázi

## Poznámka o php scriptech
Kvůli jednoduchosti testování aplikace jsem se nakonec rozhodla aplikaci k databázi 
připojit přes php script, který běží na serveru. Avšak ve zdrojových souborech 
jsem nechala i původní třídu (s názvem "MyConnectionToDatabae"), která využívala 
knihovny Javy k připojení k databázi (ta však vyžadovala lokální MySQL server 
a tedy spuštění aplikace by bylo náročné).

## Spuštění
Aplikace by měla být spustitelná po jejích setevením pomocí maven souboru.
Pro normální chod aplikace je kvůli připojení k databázi zapotřebí připojení k internetu.
O trochu podrobnější popis je k dispozici v uživatelské dokumentaci.