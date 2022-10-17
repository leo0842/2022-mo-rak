import { MemberInterface } from './group';

interface AppointmentInterface {
  id: number;
  code: string;
  title: string;
  description: string;
  durationHours: number;
  durationMinutes: number;
  startDate: string;
  endDate: string;
  startTime: string;
  endTime: string;
  closedAt: string;
  isClosed: boolean;
}

interface AppointmentRecommendationInterface {
  rank: number;
  recommendStartDateTime: string;
  recommendEndDateTime: string;
  availableMembers: Array<MemberInterface>;
  unavailableMembers: Array<MemberInterface>;
}

interface Time {
  period: 'AM' | 'PM';
  hour: string;
  minute: string;
}

type AvailableTimes = Array<{ start: string; end: string }>;

// TODO: 대문자 타입 일관성 지켜주기
// TODO: CreateAppointmentRequest 어떤지?? 만약 해주게 되면 요청 보내는 것은 다 Request, 응답은 Response로 바꿔줘야함
type createAppointmentData = Omit<AppointmentInterface, 'id' | 'code' | 'isClosed'>;

type getAppointmentResponse = AppointmentInterface & {
  isHost: boolean;
};

type getAppointmentsResponse = Array<
  Omit<AppointmentInterface, 'startDate' | 'endDate' | 'startTime' | 'endTime'> & {
    count: number;
  }
>;

type AppointmentStatus = 'CLOSED' | 'OPEN';

export {
  Time,
  createAppointmentData,
  getAppointmentsResponse,
  getAppointmentResponse,
  AvailableTimes,
  AppointmentRecommendationInterface,
  AppointmentInterface,
  AppointmentStatus
};
