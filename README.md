# mybooks
개인용 도서 관리 프로그램(개발 중).

## Specification
### 공통
* JWT
* Git

### Frontend (incl. Backend for Frontend)
* React.js
* Next.js
* Express.js (Cache 처리 한정)
* Typescript
* NextAuth
* styled-components
* React-query
* Axios
* Material UI
등

### Backend
* Java 17
* Spring Boot
* Spring Security
* Gradle
* JPA
* Lombok
* MySQL (production)
* H2 (development)
* Redis (로그인 처리용)
등

## 경로 메모
Next.js 소스는 `src/main/ui/my-app`에 있음.
​
## 설정 방법
### macOS + VScode
#### DB
홈브루 설치하고 Redis(개발, 운영 모두 해당), MySQL(운영 전용) 설치. H2(개발 전용)는 스프링 내장 DB 사용.

#### Java
홈브루 설치하고 터미널에서 아래 실행.
> brew install openjdk@17

VSCode에 Java, Spring Boot, Gradle 관련 확장 기능들 추가.

VSCode에서 설정 들어가서 jdk라고 검색 후 settings.json 편집으로 들어가서 아래 추가.
> "java.jdt.ls.java.home": "/usr/local/opt/openjdk@17",

`src/main` 디렉토리에 `resources-dev` 및 `resources-prod` 디렉토리 생성 후, `src/main/resources/sample_dev_application.yml.txt`을 수정하여 `src/main/resources-dev/application.yml`으로 저장하고, `src/main/resources/sample_prod_application.yml.txt` 을 수정하여 `src/main/resources-prod/application.yml`으로 저장할 것.

`src/main/resources/sample_launch.json.txt`을 수정하여 `.vscode/launch.json`으로 저장하고, `src/main/resources/sample_settings.json.txt`을 수정하여 `.vscode/settings.json`으로 저장할 것. 

VSCode 창 닫았다 다시 열고 좌측 실행 및 디버그 버튼 누르고 서버 실행.

#### Next.js
VScode 터미널 
> cd src/main/ui/my-app
> npm install --force

만약에 npm 버전이 안 맞으면 `nvm` 설치 후 기본 버전으로 변경 후 `npm install` 하면 됨.
> nvm install 18.12.0
> nvm alias default 18.12.0

빌드 후 서버 실행
> npx next build && node server