# WASDungeons fork (VaLycSay/1.1.0-fork)

Fork do mod abandonado [randomcmd/WASDungeons](https://github.com/randomcmd/WASDungeons) (sem release, ultimo commit 2020-07-01) para deixar ele realmente compilavel e usavel hoje.

## O que esta fork muda

- **`pom.xml` funcional** - o original nao declarava dependencia do `jnativehook` (estava so na config do IntelliJ), entao `mvn package` quebrava. Adicionado `jnativehook 2.1.0` e `maven-shade-plugin` para gerar fat-jar executavel direto.
- **`config.properties` externa** - keybinds, `mouse.recenter` e `monitor.useCurrent` viram configuraveis. Sem o arquivo, defaults sensiveis sao usados.
- **`mouse.recenter=false` por padrao** - o original sempre re-centralizava o mouse ao soltar WASD, atrapalhando mira e UI. Agora o mouse fica onde estiver. Para reativar o comportamento legado: `mouse.recenter=true`.
- **Multi-monitor** - `monitor.useCurrent=true` (default) usa o monitor onde o cursor esta. O original assumia monitor primario.
- **`Robot` singleton** - o original instanciava um novo `Robot` a cada tecla pressionada. Agora e criado uma vez no `main`.
- **`BUTTON1_DOWN_MASK`** em vez de `BUTTON1_MASK` (deprecated desde Java 9).
- **Bug do double-listener corrigido** - o original chamava `init()` em uma instancia mas registrava `new ModKeyListener()` (instancia diferente, com `robot == null`). Agora a mesma instancia inicializada e a registrada.
- **GitHub Actions** - workflow `.github/workflows/build.yml` builda o jar a cada push e publica como artifact.

## Como usar

1. Baixe o jar do **Actions tab > Build > artifacts > WASDungeons-jar** (ou builde local com `mvn package`).
2. (Opcional) Coloque o `config.properties` ao lado do jar para customizar.
3. Execute: `java -jar WASDungeons-1.1.0-fork.jar`.
4. Abra o Minecraft Dungeons. Aperte W/A/S/D para mover. Aperte `P` para pausar o mod (libera o mouse normalmente).

## O que e (explicacao do mod)

Nao e um mod do jogo - e um macro externo em Java. Ele:

1. Captura W/A/S/D globalmente via `jnativehook`.
2. Calcula um vetor 2D a partir das teclas pressionadas.
3. Move o cursor do mouse para a borda correspondente da tela e segura o botao esquerdo, simulando o point-and-click oficial do Minecraft Dungeons na direcao desejada.

Portanto: funciona com **qualquer** versao do jogo (Microsoft Store, Steam, copia portable), porque nao toca no jogo - so dirige o mouse.

## Limitacoes conhecidas

- Como ainda usa o sistema de point-and-click do jogo, **mira de tiro/arco fica imprecisa enquanto voce se move** (o cursor fica grudado na borda da tela). Para mirar com precisao, pause com `P`.
- A velocidade do personagem fica saturada (sempre 100%), nao analogica.
- Conflito potencial com programas que tambem hookeiam input global.

Se alguma dessas limitacoes te incomoda, considere usar um emulador de gamepad virtual (`VController` + `ViGEmBus`), que mapeia WASD para o stick analogico de um Xbox 360 virtual e o jogo trata como controle nativo - melhor experiencia geral.

## Licenca

Mantida igual ao upstream (sem licenca explicita). Patches deste fork: MIT.
