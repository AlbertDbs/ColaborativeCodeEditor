declare global {
  interface Window {
    google?: any;
  }
}

const GOOGLE_SCRIPT_SRC = 'https://accounts.google.com/gsi/client';
let loadingPromise: Promise<void> | null = null;

async function loadGoogleScript(): Promise<void> {
  if (window.google?.accounts?.id) return;
  if (loadingPromise) return loadingPromise;
  loadingPromise = new Promise((resolve, reject) => {
    const script = document.createElement('script');
    script.src = GOOGLE_SCRIPT_SRC;
    script.async = true;
    script.defer = true;
    script.onload = () => resolve();
    script.onerror = () => reject(new Error('Failed to load Google Identity Services.'));
    document.head.appendChild(script);
  });
  return loadingPromise;
}

/**
 * Trigger a Google sign-in and resolve with the returned ID token (credential).
 */
export async function getGoogleIdToken(): Promise<string> {
  await loadGoogleScript();
  const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID;
  if (!clientId) {
    throw new Error('Google client ID is not configured (VITE_GOOGLE_CLIENT_ID).');
  }

  return new Promise<string>((resolve, reject) => {
    let completed = false;

    const handleResponse = (response: { credential?: string }) => {
      completed = true;
      if (response && response.credential) {
        resolve(response.credential);
      } else {
        reject(new Error('Google sign-in did not return a credential.'));
      }
    };

    window.google.accounts.id.initialize({
      client_id: clientId,
      callback: handleResponse,
      cancel_on_tap_outside: true,
      use_fedcm_for_prompt: true
    });

    window.google.accounts.id.prompt((notification: any) => {
      if (notification.isNotDisplayed?.() || notification.isSkippedMoment?.()) {
        if (!completed) {
          reject(new Error('Google sign-in was dismissed.'));
        }
      }
    });

    // Safety timeout
    setTimeout(() => {
      if (!completed) reject(new Error('Google sign-in timed out.'));
    }, 20000);
  });
}
