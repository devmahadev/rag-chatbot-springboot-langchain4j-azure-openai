import FormClient from "../form-client";
import { sendToLoad } from "../actions";

export default function HomePage() {
  return <FormClient action={sendToLoad} />;
}