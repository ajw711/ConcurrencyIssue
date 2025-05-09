# 개요
이 프로젝트는 **동시성 문제**를 해결하기 위해 다양한 **분산 락** 방식을 구현한 애플리케이션
**동시성 문제**란, 여러 스레드가 동시에 동일한 자원에 접근할 때 발생할 수 있는 **경쟁 상태(Race Condition)** 문제로 데이터베이스에서의 동시성 문제를 방지한다. 

![에러](https://github.com/user-attachments/assets/7d089317-95aa-45ed-9098-5af8e3d4cf83)

# 동시성 문제 해결 방법

## 1. 비관적 락 (Pessimistic Lock)

### 개념
트랜잭션을 시작할 때 데이터를 잠그고 다른 트랜잭션이 접근할 수 없게 막는 방식으로 락이 풀릴 때까지 다른 트랜잭션은 대기합니다.

### 장점
- **동시성 문제 확실히 해결**: 여러 트랜잭션이 동시에 데이터를 변경하는 것을 방지

### 단점
- **성능 저하**: 락을 걸고 기다려야 하므로 다수의 트랜잭션이 발생할 때 성능이 크게 저하될 수 있다.
- 특히 많은 트랜잭션이 발생하는 환경에서는 성능 문제가 발생할 수 있다.

![비관적락](https://github.com/user-attachments/assets/66eeeeed-e0f8-4900-8bec-57c77114844c)
---

## 2. 낙관적 락 (Optimistic Lock)

### 개념
락을 사용하지 않고 버전 정보를 이용하여 업데이트가 실패할 경우 이를 처리하는 방식 
데이터를 읽은 후 업데이트를 시도할 때, 읽은 버전과 현재 버전이 일치하는지 확인하고, 다르면 충돌이 발생하여 업데이트를 실패시킨다.  
이때 실패하면 재시도 로직을 작성해야 한다.

### 장점
- **충돌이 발생하지 않으면 효율적**: 충돌이 발생하지 않으면 락을 걸지 않기 때문에 성능에 유리

### 단점
- **충돌 시 재시도 필요**: 업데이트 실패 시, 충돌을 처리하고 재시도하는 로직을 개발자가 직접 작성해야 한다.
- 이 로직을 구현하지 않으면 데이터 정합성에 문제가 생길 수 있다.

---

## 3. 네임디드 락 (Named Lock)

### 개념
락에 이름을 부여하여 특정 이름을 기준으로 락을 관리한다.  
예를 들어, **MySQL의 `GET_LOCK`**과 **`RELEASE_LOCK`**을 사용하거나 **JPA의 native query**를 사용하여 분산 락을 구현할 수 있다.

### 장점
- **분산 락 구현**: 여러 시스템에서 동일한 자원에 접근할 때 락을 사용할 수 있다.
- **데이터 정합성 관리**: 분산 환경에서 데이터 정합성을 유지하는 데 유용하다.

### 단점
- **트랜잭션 종료 시 락 해제 및 세션 관리**: 락을 사용한 후 트랜잭션 종료 시 락 해제를 적절히 관리하지 않으면 **데드락**이 발생할 수 있다.
- **복잡한 구현**: 락을 적절히 관리하는 방법이 복잡할 수 있으며, 시스템에 따라 구현이 까다로울 수 있다.

---

## 4. Redis의 Lettuce 라이브러리

### 개념
Lettuce는 **Redis 클라이언트**로, 분산 락을 직접 지원하지 않지만 **SETNX** 명령어를 사용하여 락을 구현할 수 있다.  
`SETNX`는 **"Set if Not Exists"** 명령어로, key에 대한 값이 없을 때만 데이터를 저장하는 방식  
이 방식을 사용하면 Spin Lock 방식처럼 **락을 획득하려는 스레드가 반복적으로 락을 시도**할 수 있다. 하지만, **retry 로직**을 개발자가 직접 작성해야 한다.

### 장점
- **분산 락 구현 가능**: Redis를 활용하여 여러 서버 간에 락을 관리할 수 있다.
- **상대적으로 단순한 구현**: 복잡한 분산 시스템에서 락을 관리할 수 있다.

### 단점
- **Retry 로직 필요**: 락을 시도하고 실패하면, 이를 **반복적으로 시도하는 로직**을 개발자가 작성해야 한다.
- **성능 저하**: Spin Lock 방식으로 락을 시도하기 때문에, 빈번한 락 실패 시 성능 저하가 발생할 수 있다.

![redis](https://github.com/user-attachments/assets/23ede6bb-d628-481c-90a0-43d2f384a67a)
---

## 5. Redis의 Redisson 라이브러리

### 개념
Redisson은 **Pub/Sub** 방식을 활용하여 분산 락을 구현  
Redisson에서 제공하는 **RLock**을 사용하면, 락을 점유한 스레드가 락을 해제하면 **대기 중인 스레드에게 이를 알리는 방식** 이를 통해 락을 얻기 위한 **retry 로직**을 별도로 구현할 필요가 없다.

### 장점
- **Pub/Sub 기반**: 락을 해제한 후, 대기 중인 스레드에게 알림을 주므로 **대기 중인 스레드**가 락을 얻을 수 있다.
- **별도 retry 로직 불필요**: 락을 획득할 때 retry 로직을 개발자가 작성할 필요가 없다.


### 단점
- **복잡성**: Redisson을 사용한 구현은 설정이 복잡할 수 있으며, Redisson 클러스터 환경에서는 추가적인 설정이 필요할 수 있다.
- **외부 의존성**: Redis를 의존하는 시스템이므로, Redis 서버가 고장나면 락 관련 문제가 발생할 수 있다.

---

